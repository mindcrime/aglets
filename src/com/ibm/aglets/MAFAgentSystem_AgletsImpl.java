package com.ibm.aglets;

/*
 * @(#)MAFAgentSystem_AgletsImpl.java
 * 
 * IBM Confidential-Restricted
 * 
 * OCO Source Materials
 * 
 * 03L7246 (c) Copyright IBM Corp. 1996, 1998
 * 
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has been
 * deposited with the U.S. Copyright Office.
 */

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OptionalDataException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Properties;

import org.aglets.log.AgletsLogger;

import com.ibm.aglet.AgletContext;
import com.ibm.aglet.AgletException;
import com.ibm.aglet.AgletProxy;
import com.ibm.aglet.InvalidAgletException;
import com.ibm.aglet.NotHandledException;
import com.ibm.aglet.message.Message;
import com.ibm.aglet.message.MessageException;
import com.ibm.aglet.system.AgletRuntime;
import com.ibm.awb.misc.Resource;
import com.ibm.maf.AgentIsRunning;
import com.ibm.maf.AgentIsSuspended;
import com.ibm.maf.AgentNotFound;
import com.ibm.maf.AgentProfile;
import com.ibm.maf.AgentStatus;
import com.ibm.maf.AgentSystemInfo;
import com.ibm.maf.ArgumentInvalid;
import com.ibm.maf.AuthInfo;
import com.ibm.maf.ClassName;
import com.ibm.maf.ClassUnknown;
import com.ibm.maf.DeserializationFailed;
import com.ibm.maf.EntryNotFound;
import com.ibm.maf.FinderNotFound;
import com.ibm.maf.LanguageMap;
import com.ibm.maf.MAFAgentSystem;
import com.ibm.maf.MAFExtendedException;
import com.ibm.maf.MAFFinder;
import com.ibm.maf.MessageEx;
import com.ibm.maf.Name;
import com.ibm.maf.NotHandled;
import com.ibm.maf.ResumeFailed;
import com.ibm.maf.SuspendFailed;
import com.ibm.maf.TerminateFailed;
import com.ibm.maf.rmi.MAFFinder_RMIImpl;

public class MAFAgentSystem_AgletsImpl extends MAFAgentSystem {

    static final short AGLETS = 1;
    static final short JAVA = 1;
    static final short JAVA_SERIALIZATION = 1;
    static private LanguageMap[] map = new LanguageMap[1];
    static private String agletsPublicRoot;
    static private String[][] agletsPublicAliases;

    static private final String ALIASES_SEP = " -> ";

    static private String _finder_host = null;
    static private String _finder_port = null;
    static private String _finder_name = null;
    static private MAFFinder _finder = null;
    private static AgletsLogger logger = AgletsLogger.getLogger(MAFAgentSystem_AgletsImpl.class.getName());

    static {
	try {
	    Resource aglets_res = Resource.getResourceFor("aglets");
	    short[] ser = new short[1];

	    ser[0] = JAVA_SERIALIZATION;
	    map[0] = new LanguageMap(JAVA, ser);

	    /*
	     * =======================================================
	     * Initialize Variables
	     * ========================================================
	     */

	    /*
	     * Creating public root
	     */
	    String publicRoot = aglets_res.getString("aglets.public.root");

	    if (publicRoot != null) {
		agletsPublicRoot = publicRoot;

		// The link problem has been solved? M.O.-> ONONO
		// agletsPublicRoot = new File(publicRoot).getCanonicalPath();
	    }

	    // System.out.println("exportPath = " + exportPath);
	    // agletsExportPath = FileUtils.localize(exportpath);

	    agletsPublicAliases = null;
	    String publicAliases[] = aglets_res.getStringArray("aglets.public.aliases", ",");

	    if ((publicAliases != null) && (publicAliases.length > 0)) {
		agletsPublicAliases = new String[publicAliases.length][];

		for (int i = 0; i < publicAliases.length; i++) {
		    int idx = publicAliases[i].indexOf(ALIASES_SEP);
		    String ali_name = publicAliases[i].substring(0, idx);
		    String ali_path = publicAliases[i].substring(idx
			    + ALIASES_SEP.length());

		    // Ensures alias name("ali_name") ends with "/".
		    if (!ali_name.endsWith("/")) {
			ali_name = ali_name + "/";
		    }

		    // Ensures alias path("ali_path") ends with separator.
		    if (!ali_path.endsWith(File.separator)) {
			ali_path = ali_path + File.separator;
		    }

		    agletsPublicAliases[i] = new String[2];
		    agletsPublicAliases[i][0] = ali_name;
		    agletsPublicAliases[i][1] = ali_path;
		}
	    }

	    // Get "maf.finder.*" properties
	    Properties props = System.getProperties();

	    _finder_host = props.getProperty("maf.finder.host", null);
	    _finder_port = props.getProperty("maf.finder.port", Integer.toString(MAFFinder_RMIImpl.REGISTRY_PORT));
	    _finder_name = props.getProperty("maf.finder.name", MAFFinder_RMIImpl.REGISTRY_NAME);
	} catch (Throwable t) {
	    t.printStackTrace();
	}
    }

    private AgletRuntime _runtime = null;
    private AgentSystemInfo _agent_system_info = null;
    private String _address;

    String export_dir = "";

    public MAFAgentSystem_AgletsImpl(AgletRuntime runtime) {
	Name name = MAF.toAgentSystemName(this, runtime.getOwnerCertificate());

	this._agent_system_info = new AgentSystemInfo(name, AGLETS, map, "Aglets Server beta2", (short) 0, (short) 2, null);
	this._runtime = runtime;
    }

    /**
	 * 
	 */
    private void checkProfile(AgentProfile agent_profile)
							 throws MAFExtendedException {
	if (this._agent_system_info.agent_system_type == agent_profile.agent_system_type) {
	    for (LanguageMap element : map) {
		if (element.language_id == agent_profile.language_id) {
		    for (short serialization : element.serializations) {
			if (serialization == agent_profile.serialization) {
			    return;
			}
		    }
		}
	    }
	}
	throw new MAFExtendedException("AgentProfile doesn't match");
    }

    @Override
    public Name create_agent(
			     Name agent_name,
			     AgentProfile agent_profile,
			     byte[] agent,
			     String place_name,
			     Object[] arguments,
			     ClassName[] class_names,
			     String code_base,
			     MAFAgentSystem class_provider)
							   throws ClassUnknown,
							   ArgumentInvalid,
							   DeserializationFailed,
							   MAFExtendedException {

	this.checkProfile(agent_profile);

	AgletContext context = this._runtime.getAgletContext(place_name);

	if (context == null) {
	    throw new MAFExtendedException("Context Not Found:" + place_name);
	}
	try {
	    URL real_cb = code_base == null ? null : new URL(code_base);
	    AgletProxy new_aglet = context.createAglet(real_cb, class_names[0].name, arguments[0]);

	    return new Name(agent_name.authority, new_aglet.getAgletID().toByteArray(), agent_name.agent_system_type);
	} catch (MalformedURLException ex) {
	    throw new MAFExtendedException("Invalid CodeBase:" + code_base);
	} catch (ClassNotFoundException ex) {
	    throw new ClassUnknown("Class Not Found:" + ex.getMessage());
	} catch (IOException ex) {
	    throw new MAFExtendedException("IO Failed:" + ex.getMessage());
	} catch (AgletException ex) {
	    ex.printStackTrace();
	    throw new MAFExtendedException("Aglet Exception:" + ex.getMessage());
	} catch (InstantiationException ex) {
	    throw new MAFExtendedException("Instantiation Failed:"
		    + ex.getMessage());
	} catch (RuntimeException ex) {
	    ex.printStackTrace();
	    throw ex;
	}
    }

    @Override
    public byte[][] fetch_class(
				ClassName[] class_name_list,
				String code_base,
				AgentProfile agent_profile)
							   throws ClassUnknown,
							   MAFExtendedException {

	this.checkProfile(agent_profile);

	try {
	    String local_file = this.getLocalFile(code_base);
	    String filename = this.getFileName(local_file);

	    if (class_name_list != null) {
		if (class_name_list.length != 1) {
		    throw new MAFExtendedException("Multiple classes not supported");
		}
		if (filename.endsWith("/") == false) {
		    filename += "/";
		}
		filename += class_name_list[0];
	    }

	    // System.out.println("fetch: " + filename);

	    byte[][] ret = new byte[1][];

	    ret[0] = this.readData(filename);
	    return ret;
	} catch (IOException ex) {
	    ex.printStackTrace();
	    throw new ClassUnknown("Codebase Invalid:" + code_base);
	}
    }

    @Override
    public String find_nearby_agent_system_of_profile(AgentProfile profile)
									   throws EntryNotFound {
	try {
	    this.checkProfile(profile);
	} catch (MAFExtendedException ex) {
	    throw new EntryNotFound("MAFExtendedException ex");
	}
	return "";
    }

    @Override
    public AgentStatus get_agent_status(Name agent_name) throws AgentNotFound {
	return null;
    }

    @Override
    public AgentSystemInfo get_agent_system_info() {
	return this._agent_system_info;
    }

    @Override
    public AuthInfo get_authinfo(Name agent_name) throws AgentNotFound {
	return null;
    }

    @Override
    public MAFFinder get_MAFFinder() throws FinderNotFound {
	if ((_finder == null) && (_finder_host != null)) {
	    try {
		_finder = (MAFFinder) java.rmi.Naming.lookup("rmi://"
			+ _finder_host + ":" + _finder_port + "/"
			+ _finder_name);
	    } catch (Exception ex) {

		// ex.printStackTrace();
		throw new FinderNotFound();
	    }
	}
	return _finder;
    }

    @Override
    public String getAddress() {
	return this._address;
    }

    private AgletContextImpl getAgletContext(Name agent_name) {
	if (agent_name == null) {
	    return null;
	}

	byte[] auth = agent_name.authority;

	// System.out.println("Authority = "+Hexadecimal.valueOf(auth));

	if ((auth != null) && (auth.length > 0)) {

	    // agent_name indicates an agent if authority is not null.
	    // see MessageBroker#sendMessage
	    return null;
	}

	// agent_name indicates an AgletContext only if authority is null.
	// see MessageBroker#sendMessage
	String placename = null;

	try {
	    byte[] id = agent_name.identity;

	    // System.out.println("Identity = "+Hexadecimal.valueOf(id));
	    placename = new String(id);
	} catch (Exception excpt) {
	    return null;
	}

	return (AgletContextImpl) this._runtime.getAgletContext(placename);
    }

    private String getFileName(String local_file) {
	String resolvedName = this.getResolvedName(local_file);

	return resolvedName.replace('/', File.separatorChar);
    }

    private LocalAgletRef getLocalAgletRef(Name agent_name)
							   throws AgentNotFound {
	LocalAgletRef ref = LocalAgletRef.getAgletRef(agent_name);

	if (ref == null) {
	    throw new AgentNotFound("NotFound");
	}
	return ref;
    }

    private String getLocalFile(String code_base) throws ClassUnknown {
	try {
	    return new URL(code_base).getFile();
	} catch (MalformedURLException ex) {
	    throw new ClassUnknown("Codebase Invalid:" + code_base);
	}
    }

    private String getResolvedName(String local_file) {
	if (agletsPublicAliases != null) {
	    for (String[] agletsPublicAliase : agletsPublicAliases) {
		String ali_name = agletsPublicAliase[0];
		String ali_path = agletsPublicAliase[1];

		if (local_file.startsWith(ali_name)) {
		    local_file = local_file.substring(ali_name.length());
		    return ali_path + local_file;
		}
	    }
	}

	// if (local_file.startsWith(File.separator) && local_file.length() > 1)
	// {
	// local_file = local_file.substring(1);
	// }
	return agletsPublicRoot + local_file;
    }

    @Override
    public Name[] list_all_agents() {
	return null;
    }

    @Override
    public Name[] list_all_agents_of_authority(byte[] authority) {
	return null;
    }

    @Override
    public String[] list_all_places() {
	AgletContext[] contexts = this._runtime.getAgletContexts();
	String list[] = new String[contexts.length];

	for (int i = 0; i < list.length; i++) {
	    list[i] = contexts[i].getName();
	}
	return list;
    }

    private byte[] readData(String filename) throws IOException {

	final String fFilename = filename;
	final File fFile = new File(filename);

	try {
	    return (byte[]) AccessController.doPrivileged(new PrivilegedExceptionAction() {
		public Object run() throws IOException {
		    if (fFile.exists() == false) {
			throw new IOException("FileNotFound: " + fFile);
		    }
		    if (fFile.canRead() == false) {
			throw new IOException("FileNotReadable: " + fFile);
		    }

		    // -- Get length of byte code.
		    int length = (int) fFile.length();
		    byte[] bytecode = new byte[length];

		    // -- get byte code
		    FileInputStream in = new FileInputStream(fFilename);
		    int offset = 0;

		    while (length > 0) {
			int read = in.read(bytecode, offset, length);

			offset += read;
			length -= read;
		    }
		    in.close();

		    return bytecode;
		}
	    });
	} catch (PrivilegedActionException ex) {
	    throw (IOException) ex.getException();
	}
    }

    @Override
    public void receive_agent(
			      Name agent_name,
			      AgentProfile agent_profile,
			      byte[] agent,
			      String place_name,
			      ClassName[] class_names,
			      String code_base,
			      MAFAgentSystem sender)
						    throws ClassUnknown,
						    DeserializationFailed,
						    MAFExtendedException {

	this.checkProfile(agent_profile);

	final AgletContextImpl impl = (AgletContextImpl) this._runtime.getAgletContext(place_name);

	if (impl == null) {
	    System.out.println("Place not found " + place_name);
	    throw new MAFExtendedException("Place not found");
	}

	final Name an = agent_name;
	final ClassName[] cn = class_names;
	final String cb = code_base;
	final byte[] a = agent;
	final String sa = sender.getAddress();

	try {
	    AccessController.doPrivileged(new PrivilegedExceptionAction() {
		public Object run() throws Exception {
		    impl.receiveAglet(an, cn, cb, a, sa);
		    return null;
		}
	    });
	} catch (PrivilegedActionException excpt) {
	    Exception ex = excpt.getException();

	    if (ex instanceof ClassNotFoundException) {
		throw new ClassUnknown("Class Not Found:" + ex.getMessage());
	    } else if (ex instanceof AgletException) {
		throw new MAFExtendedException("");
	    }
	}
    }

    @Override
    public long receive_future_message(
				       Name agent_name,
				       byte[] raw_msg,
				       MAFAgentSystem message_sender)
								     throws AgentNotFound,
								     ClassUnknown,
								     DeserializationFailed,
								     MAFExtendedException {
	LocalAgletRef ref = this.getLocalAgletRef(agent_name);
	long return_id = System.currentTimeMillis();

	try {
	    Message message = (Message) MessageInputStream.toObject(ref.resourceManager, raw_msg);

	    FutureReplyImpl future = new RemoteFutureReplyImpl(message_sender, ref.resourceManager, return_id);

	    ref.sendFutureMessage(message, future);

	} catch (OptionalDataException ex) {
	    throw new DeserializationFailed(toMessage(ex));

	} catch (IOException ex) {
	    throw new DeserializationFailed(toMessage(ex));

	} catch (ClassNotFoundException ex) {
	    throw new ClassUnknown(toMessage(ex));

	} catch (InvalidAgletException ex) {
	    throw new AgentNotFound(toMessage(ex));

	}

	return return_id;
    }

    @Override
    public void receive_future_reply(long return_id, byte[] reply)
								  throws EntryNotFound,
								  ClassUnknown,
								  DeserializationFailed,
								  MAFExtendedException {
	Long l = new Long(return_id);

	ResourceManager rm = MessageBroker.getResourceManager(l);

	if (rm == null) {
	    throw new EntryNotFound("Invalid return id");
	}

	FutureReplyImpl future = MessageBroker.getFutureReply(l);

	try {
	    Object obj[] = (Object[]) MessageInputStream.toObject(rm, reply);

	    switch (((Integer) obj[0]).intValue()) {
	    case 0: // HANDLED
		future.sendReplyIfNeeded(obj[1]);
		break;
	    case 1: // MESSAGE_EXCEPTION
		MessageException ex = (MessageException) obj[1];

		future.sendExceptionIfNeeded(ex.getException());
		break;
	    case 2: // NOT_HANDLED
		future.cancel((String) obj[1]);
		break;
	    }
	} catch (OptionalDataException ex) {
	    throw new DeserializationFailed(toMessage(ex));

	} catch (IOException ex) {
	    ex.printStackTrace();
	    throw new DeserializationFailed(toMessage(ex));

	} catch (ClassNotFoundException ex) {
	    throw new ClassUnknown(toMessage(ex));

	}
    }

    /**
     * Messaging
     */

    @Override
    public byte[] receive_message(Name agent_name, byte[] raw_msg)
								  throws AgentNotFound,
								  NotHandled,
								  MessageEx,
								  ClassUnknown,
								  DeserializationFailed,
								  MAFExtendedException {
	logger.debug("receive_message()++");
	try {

	    // context only supports only synchronized message.
	    AgletContextImpl cxt = this.getAgletContext(agent_name);

	    // not so good!
	    if (cxt != null) {
		ResourceManagerFactory rmf = cxt.getResourceManagerFactory();
		ResourceManager rm = rmf.getCurrentResourceManager();
		Message message = (Message) MessageInputStream.toObject(rm, raw_msg);

		Object ret = cxt.handleMessage(message);

		return MessageOutputStream.toByteArray(rm, ret);

	    } else {
		LocalAgletRef ref = this.getLocalAgletRef(agent_name);

		Message message = (Message) MessageInputStream.toObject(ref.resourceManager, raw_msg);

		Object ret;

		// REMIND: ad hoc.
		if (message instanceof com.ibm.awb.misc.CGIMessage) {
		    ByteArrayOutputStream o = new ByteArrayOutputStream();

		    message.setArg("cgi-response", o);
		    ret = ref.sendMessage(message);
		    return o.toByteArray();

		} else if (message.sameKind("_getAgletInfo")) {
		    ret = ref.getAgletInfo();
		} else {
		    ret = ref.sendMessage(message);
		}
		return MessageOutputStream.toByteArray(ref.resourceManager, ret);
	    }

	} catch (MessageException ex) {
	    logger.error(ex);
	    throw new MessageEx(ex.getMessage(), ex.getException());

	} catch (NotHandledException ex) {
	    logger.error(ex);
	    throw new NotHandled(toMessage(ex));

	} catch (OptionalDataException ex) {
	    logger.error(ex);
	    throw new DeserializationFailed(toMessage(ex));

	} catch (IOException ex) {
	    logger.error(ex);
	    ex.printStackTrace();
	    throw new DeserializationFailed(toMessage(ex));

	} catch (ClassNotFoundException ex) {
	    logger.error(ex);
	    throw new ClassUnknown(toMessage(ex));

	} catch (InvalidAgletException ex) {
	    logger.error(ex);
	    throw new AgentNotFound(toMessage(ex));

	}
    }

    @Override
    public void receive_oneway_message(Name agent_name, byte[] raw_msg)
								       throws AgentNotFound,
								       ClassUnknown,
								       DeserializationFailed,
								       MAFExtendedException {

	LocalAgletRef ref = this.getLocalAgletRef(agent_name);

	try {
	    Message msg = (Message) MessageInputStream.toObject(ref.resourceManager, raw_msg);

	    ref.sendOnewayMessage(msg);

	} catch (OptionalDataException ex) {
	    logger.error(ex);
	    throw new DeserializationFailed(toMessage(ex));

	} catch (IOException ex) {
	    logger.error(ex);
	    throw new DeserializationFailed(toMessage(ex));

	} catch (ClassNotFoundException ex) {
	    logger.error(ex);
	    throw new ClassUnknown(toMessage(ex));

	} catch (InvalidAgletException ex) {
	    logger.error(ex);
	    throw new AgentNotFound(toMessage(ex));

	}
    }

    @Override
    public void resume_agent(Name agent_name)
					     throws AgentNotFound,
					     ResumeFailed,
					     AgentIsRunning {
	return;
    }

    @Override
    public byte[] retract_agent(Name agent_name)
						throws AgentNotFound,
						MAFExtendedException {

	LocalAgletRef ref = this.getLocalAgletRef(agent_name);

	if (ref == null) {
	    throw new AgentNotFound("NotFound");
	}

	return ref.retract();
    }

    @Override
    synchronized public void setAddress(String name) {
	if (this._address != null) {
	    throw new IllegalArgumentException("Address already set");
	}

	this._address = name;
    }

    @Override
    public void suspend_agent(Name agent_name)
					      throws AgentNotFound,
					      SuspendFailed,
					      AgentIsSuspended {
	return;
    }

    @Override
    public void terminate_agent(Name agent_name)
						throws AgentNotFound,
						TerminateFailed {
	return;
    }

    static String toMessage(Exception ex) {
	return ex.getClass().getName() + ':' + ex.getMessage();
    }
}
