package com.ibm.maf.rmi;

/*
 * @(#)MAFAgentSystem_RMIClient.java
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

import java.rmi.ConnectException;
import java.rmi.MarshalException;
import java.rmi.RemoteException;
import java.rmi.UnmarshalException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

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
import com.ibm.maf.MAFAgentSystem;
import com.ibm.maf.MAFExtendedException;
import com.ibm.maf.MAFFinder;
import com.ibm.maf.MessageEx;
import com.ibm.maf.Name;
import com.ibm.maf.NotHandled;
import com.ibm.maf.ResumeFailed;
import com.ibm.maf.SuspendFailed;
import com.ibm.maf.TerminateFailed;

public final class MAFAgentSystem_RMIClient extends MAFAgentSystem {

    private MAFAgentSystem_RMI _agent_system = null;
    private String _address = null;

    static java.util.Hashtable to_maf = new java.util.Hashtable();

    MAFAgentSystem_RMIClient(MAFAgentSystem_RMI __rmi, String address) {
	this._agent_system = __rmi;
	this._address = address;
	if (this._address != null) {
	    to_maf.put(__rmi, this);
	}
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
    MAFExtendedException

    /* RequestRefused */
    {
	try {
	    final MAFAgentSystem_RMI fAgentSystem = this._agent_system;
	    final Name fAgentName = agent_name;
	    final AgentProfile fAgentProfile = agent_profile;
	    final byte[] fAgent = agent;
	    final String fPlaceName = place_name;
	    final Object[] fArguments = arguments;
	    final ClassName[] fClassNames = class_names;
	    final String fCodeBase = code_base;
	    final MAFAgentSystem fClassProvider = class_provider;

	    return (Name) AccessController.doPrivileged(new PrivilegedExceptionAction() {
		@Override
		public Object run() throws RemoteException {
		    MAFAgentSystem_RMI rmi_class_provider = to_rmi_agentsystem(fClassProvider);

		    while (true) {
			try {
			    return fAgentSystem.create_agent(fAgentName, fAgentProfile, fAgent, fPlaceName, fArguments, fClassNames, fCodeBase, rmi_class_provider);
			} catch (ConnectException ex) {
			    MAFAgentSystem_RMIClient.this.rebind(ex);
			} catch (UnmarshalException ex) {
			    MAFAgentSystem_RMIClient.this.rebind(ex);
			} catch (MarshalException ex) {
			    MAFAgentSystem_RMIClient.this.rebind(ex);
			}
		    }
		}
	    });
	} catch (PrivilegedActionException ex) {
	    RemoteException ee = (RemoteException) ex.getException();

	    if (ee.detail instanceof ClassUnknown) {
		throw (ClassUnknown) ee.detail;
	    } else if (ee.detail instanceof ArgumentInvalid) {
		throw (ArgumentInvalid) ee.detail;
	    } else if (ee.detail instanceof DeserializationFailed) {
		throw (DeserializationFailed) ee.detail;
	    } else if (ee.detail instanceof MAFExtendedException) {
		throw (MAFExtendedException) ee.detail;
	    } else if (ee.detail instanceof RuntimeException) {
		throw (RuntimeException) ee.detail;
	    } else {
		ex.printStackTrace();
		throw new RuntimeException(ee.getMessage());
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	    return null;
	}
    }

    @Override
    public byte[][] fetch_class(
                                ClassName[] class_name_list,
                                String code_base,
                                AgentProfile agent_profile)
    throws ClassUnknown,
    MAFExtendedException

    /* , RequestRefused */
    {
	try {
	    final MAFAgentSystem_RMI fAgentSystem = this._agent_system;
	    final ClassName[] fClassNameList = class_name_list;
	    final String fCodeBase = code_base;
	    final AgentProfile fAgentProfile = agent_profile;

	    return (byte[][]) AccessController.doPrivileged(new PrivilegedExceptionAction() {
		@Override
		public Object run() throws RemoteException {
		    while (true) {
			try {
			    return fAgentSystem.fetch_class(fClassNameList, fCodeBase, fAgentProfile);
			} catch (ConnectException ex) {
			    MAFAgentSystem_RMIClient.this.rebind(ex);
			} catch (UnmarshalException ex) {
			    MAFAgentSystem_RMIClient.this.rebind(ex);
			} catch (MarshalException ex) {
			    MAFAgentSystem_RMIClient.this.rebind(ex);
			}
		    }
		}
	    });
	} catch (PrivilegedActionException ex) {
	    RemoteException ee = (RemoteException) ex.getException();

	    if (ee.detail instanceof ClassUnknown) {
		throw (ClassUnknown) ee.detail;
	    } else if (ee.detail instanceof MAFExtendedException) {
		throw (MAFExtendedException) ee.detail;
	    } else if (ee.detail instanceof RuntimeException) {
		throw (RuntimeException) ee.detail;
	    } else {
		ex.printStackTrace();
		throw new RuntimeException(ee.getMessage());
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	    return null;
	}
    }

    static synchronized MAFAgentSystem find_maf_agentsystem(
                                                            MAFAgentSystem_RMI __rmi,
                                                            String address) {
	if (__rmi == null) {
	    return null;
	}

	MAFAgentSystem maf = (MAFAgentSystem) to_maf.get(__rmi);

	if (maf == null) { // && address != null) {
	    maf = new MAFAgentSystem_RMIClient(__rmi, address);
	}
	return maf;
    }

    @Override
    public String find_nearby_agent_system_of_profile(AgentProfile profile)
    throws EntryNotFound {
	try {
	    final MAFAgentSystem_RMI fAgentSystem = this._agent_system;
	    final AgentProfile fProfile = profile;

	    return (String) AccessController.doPrivileged(new PrivilegedExceptionAction() {
		@Override
		public Object run() throws RemoteException {
		    while (true) {
			try {
			    return fAgentSystem.find_nearby_agent_system_of_profile(fProfile);
			} catch (ConnectException ex) {
			    MAFAgentSystem_RMIClient.this.rebind(ex);
			} catch (UnmarshalException ex) {
			    MAFAgentSystem_RMIClient.this.rebind(ex);
			} catch (MarshalException ex) {
			    MAFAgentSystem_RMIClient.this.rebind(ex);
			}
		    }
		}
	    });
	} catch (PrivilegedActionException ex) {
	    RemoteException ee = (RemoteException) ex.getException();

	    if (ee.detail instanceof EntryNotFound) {
		throw (EntryNotFound) ee.detail;
	    } else if (ee.detail instanceof RuntimeException) {
		throw (RuntimeException) ee.detail;
	    } else {
		ex.printStackTrace();
		throw new RuntimeException(ee.getMessage());
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	    return null;
	}
    }

    @Override
    public AgentStatus get_agent_status(Name agent_name) throws AgentNotFound {
	try {
	    final MAFAgentSystem_RMI fAgentSystem = this._agent_system;
	    final Name fAgentName = agent_name;

	    return (AgentStatus) AccessController.doPrivileged(new PrivilegedExceptionAction() {
		@Override
		public Object run() throws RemoteException {
		    while (true) {
			try {
			    return fAgentSystem.get_agent_status(fAgentName);
			} catch (ConnectException ex) {
			    MAFAgentSystem_RMIClient.this.rebind(ex);
			} catch (UnmarshalException ex) {
			    MAFAgentSystem_RMIClient.this.rebind(ex);
			} catch (MarshalException ex) {
			    MAFAgentSystem_RMIClient.this.rebind(ex);
			}
		    }
		}
	    });
	} catch (PrivilegedActionException ex) {
	    RemoteException ee = (RemoteException) ex.getException();

	    if (ee.detail instanceof AgentNotFound) {
		throw (AgentNotFound) ee.detail;
	    } else if (ee.detail instanceof RuntimeException) {
		throw (RuntimeException) ee.detail;
	    } else {
		ex.printStackTrace();
		throw new RuntimeException(ee.getMessage());
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	    return null;
	}
    }

    @Override
    public AgentSystemInfo get_agent_system_info() {
	try {
	    final MAFAgentSystem_RMI fAgentSystem = this._agent_system;

	    return (AgentSystemInfo) AccessController.doPrivileged(new PrivilegedExceptionAction() {
		@Override
		public Object run() throws RemoteException {
		    while (true) {
			try {
			    return fAgentSystem.get_agent_system_info();
			} catch (ConnectException ex) {
			    MAFAgentSystem_RMIClient.this.rebind(ex);
			} catch (UnmarshalException ex) {
			    MAFAgentSystem_RMIClient.this.rebind(ex);
			} catch (MarshalException ex) {
			    MAFAgentSystem_RMIClient.this.rebind(ex);
			}
		    }
		}
	    });
	} catch (PrivilegedActionException ex) {
	    RemoteException ee = (RemoteException) ex.getException();

	    if (ee.detail instanceof RuntimeException) {
		throw (RuntimeException) ee.detail;
	    } else {
		ex.printStackTrace();
		throw new RuntimeException(ee.getMessage());
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	    return null;
	}
    }

    @Override
    public AuthInfo get_authinfo(Name agent_name) throws AgentNotFound {
	try {
	    final MAFAgentSystem_RMI fAgentSystem = this._agent_system;
	    final Name fAgentName = agent_name;

	    return (AuthInfo) AccessController.doPrivileged(new PrivilegedExceptionAction() {
		@Override
		public Object run() throws RemoteException {
		    while (true) {
			try {
			    return fAgentSystem.get_authinfo(fAgentName);
			} catch (ConnectException ex) {
			    MAFAgentSystem_RMIClient.this.rebind(ex);
			} catch (UnmarshalException ex) {
			    MAFAgentSystem_RMIClient.this.rebind(ex);
			} catch (MarshalException ex) {
			    MAFAgentSystem_RMIClient.this.rebind(ex);
			}
		    }
		}
	    });
	} catch (PrivilegedActionException ex) {
	    RemoteException ee = (RemoteException) ex.getException();

	    if (ee.detail instanceof AgentNotFound) {
		throw (AgentNotFound) ee.detail;
	    } else if (ee.detail instanceof RuntimeException) {
		throw (RuntimeException) ee.detail;
	    } else {
		ex.printStackTrace();
		throw new RuntimeException(ee.getMessage());
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	    return null;
	}
    }

    @Override
    public MAFFinder get_MAFFinder() throws FinderNotFound {
	try {
	    final MAFAgentSystem_RMI fAgentSystem = this._agent_system;

	    return (MAFFinder) AccessController.doPrivileged(new PrivilegedExceptionAction() {
		@Override
		public Object run() throws RemoteException {
		    while (true) {
			try {
			    return fAgentSystem.get_MAFFinder();
			} catch (ConnectException ex) {
			    MAFAgentSystem_RMIClient.this.rebind(ex);
			} catch (UnmarshalException ex) {
			    MAFAgentSystem_RMIClient.this.rebind(ex);
			} catch (MarshalException ex) {
			    MAFAgentSystem_RMIClient.this.rebind(ex);
			}
		    }
		}
	    });
	} catch (PrivilegedActionException ex) {
	    RemoteException ee = (RemoteException) ex.getException();

	    if (ee.detail instanceof FinderNotFound) {
		throw (FinderNotFound) ee.detail;
	    } else if (ee.detail instanceof RuntimeException) {
		throw (RuntimeException) ee.detail;
	    } else {
		ex.printStackTrace();
		throw new RuntimeException(ee.getMessage());
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	    return null;
	}
    }

    @Override
    public String getAddress() {
	return this._address;
    }

    static public MAFAgentSystem_RMI getMAFAgentSystem_RMI(String address)
    throws java.io.IOException {
	return to_rmi_agentsystem(MAFAgentSystem.getMAFAgentSystem(address));
    }

    @Override
    public Name[] list_all_agents() {
	try {
	    final MAFAgentSystem_RMI fAgentSystem = this._agent_system;

	    return (Name[]) AccessController.doPrivileged(new PrivilegedExceptionAction() {
		@Override
		public Object run() throws RemoteException {
		    while (true) {
			try {
			    return fAgentSystem.list_all_agents();
			} catch (ConnectException ex) {
			    MAFAgentSystem_RMIClient.this.rebind(ex);
			} catch (UnmarshalException ex) {
			    MAFAgentSystem_RMIClient.this.rebind(ex);
			} catch (MarshalException ex) {
			    MAFAgentSystem_RMIClient.this.rebind(ex);
			}
		    }
		}
	    });
	} catch (PrivilegedActionException ex) {
	    RemoteException ee = (RemoteException) ex.getException();

	    if (ee.detail instanceof RuntimeException) {
		throw (RuntimeException) ee.detail;
	    } else {
		ex.printStackTrace();
		throw new RuntimeException(ee.getMessage());
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	    return null;
	}
    }

    @Override
    public Name[] list_all_agents_of_authority(byte[] authority) {
	try {
	    final MAFAgentSystem_RMI fAgentSystem = this._agent_system;
	    final byte[] fAuthority = authority;

	    return (Name[]) AccessController.doPrivileged(new PrivilegedExceptionAction() {
		@Override
		public Object run() throws RemoteException {
		    while (true) {
			try {
			    return fAgentSystem.list_all_agents_of_authority(fAuthority);
			} catch (ConnectException ex) {
			    MAFAgentSystem_RMIClient.this.rebind(ex);
			} catch (UnmarshalException ex) {
			    MAFAgentSystem_RMIClient.this.rebind(ex);
			} catch (MarshalException ex) {
			    MAFAgentSystem_RMIClient.this.rebind(ex);
			}
		    }
		}
	    });
	} catch (PrivilegedActionException ex) {
	    RemoteException ee = (RemoteException) ex.getException();

	    if (ee.detail instanceof RuntimeException) {
		throw (RuntimeException) ee.detail;
	    } else {
		ex.printStackTrace();
		throw new RuntimeException(ee.getMessage());
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	    return null;
	}
    }

    @Override
    public String[] list_all_places() {
	try {
	    final MAFAgentSystem_RMI fAgentSystem = this._agent_system;

	    return (String[]) AccessController.doPrivileged(new PrivilegedExceptionAction() {
		@Override
		public Object run() throws RemoteException {
		    while (true) {
			try {
			    return fAgentSystem.list_all_places();
			} catch (ConnectException ex) {
			    MAFAgentSystem_RMIClient.this.rebind(ex);
			} catch (UnmarshalException ex) {
			    MAFAgentSystem_RMIClient.this.rebind(ex);
			} catch (MarshalException ex) {
			    MAFAgentSystem_RMIClient.this.rebind(ex);
			}
		    }
		}
	    });
	} catch (PrivilegedActionException ex) {
	    RemoteException ee = (RemoteException) ex.getException();

	    if (ee.detail instanceof RuntimeException) {
		throw (RuntimeException) ee.detail;
	    } else {
		ex.printStackTrace();
		throw new RuntimeException(ee.getMessage());
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	    return null;
	}
    }

    /* synchronized */
    private void rebind(RemoteException ex) throws RemoteException {

	/*
	 * if (ex.detail instanceof java.io.IOException == false) {
	 * ex.printStackTrace(); String msg = ex.detail.getMessage(); throw new
	 * RemoteException(msg,new MAFExtendedException(msg)); }
	 */
	MAFAgentSystem_RMI new_rmi = Handler.rebind(this._agent_system);

	if (new_rmi != null) {
	    this._agent_system = new_rmi;
	    to_maf.put(new_rmi, this);
	} else {
	    String msg = "ServerNotFound";

	    throw new RemoteException(msg, new MAFExtendedException(msg));
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
                              MAFAgentSystem class_sender)
    throws ClassUnknown,
    DeserializationFailed,
    MAFExtendedException

    /* RequestRefused */
    {
	try {
	    final MAFAgentSystem_RMI fAgentSystem = this._agent_system;
	    final Name fAgentName = agent_name;
	    final AgentProfile fAgentProfile = agent_profile;
	    final byte[] fAgent = agent;
	    final String fPlaceName = place_name;
	    final ClassName[] fClassNames = class_names;
	    final String fCodeBase = code_base;
	    final MAFAgentSystem fClassSender = class_sender;

	    AccessController.doPrivileged(new PrivilegedExceptionAction() {
		@Override
		public Object run() throws RemoteException {
		    MAFAgentSystem_RMI rmi_class_sender = to_rmi_agentsystem(fClassSender);

		    while (true) {
			try {
			    fAgentSystem.receive_agent(fAgentName, fAgentProfile, fAgent, fPlaceName, fClassNames, fCodeBase, rmi_class_sender);
			    return null;
			} catch (ConnectException ex) {
			    MAFAgentSystem_RMIClient.this.rebind(ex);
			} catch (UnmarshalException ex) {
			    MAFAgentSystem_RMIClient.this.rebind(ex);
			} catch (MarshalException ex) {
			    MAFAgentSystem_RMIClient.this.rebind(ex);
			}
		    }
		}
	    });
	} catch (PrivilegedActionException ex) {
	    RemoteException ee = (RemoteException) ex.getException();

	    if (ee.detail instanceof ClassUnknown) {
		throw (ClassUnknown) ee.detail;
	    } else if (ee.detail instanceof DeserializationFailed) {
		throw (DeserializationFailed) ee.detail;
	    } else if (ee.detail instanceof MAFExtendedException) {
		throw (MAFExtendedException) ee.detail;
	    } else if (ee.detail instanceof RuntimeException) {
		throw (RuntimeException) ee.detail;
	    } else {
		ex.printStackTrace();
		throw new RuntimeException(ee.getMessage());
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	    return;
	}
    }

    @Override
    public long receive_future_message(
                                       Name agent_name,
                                       byte[] msg,
                                       MAFAgentSystem message_sender)
    throws AgentNotFound,
    MAFExtendedException {
	try {
	    final MAFAgentSystem_RMI fAgentSystem = this._agent_system;
	    final Name fAgentName = agent_name;
	    final byte[] fMsg = msg;
	    final MAFAgentSystem fMessageSender = message_sender;

	    Long result = (Long) AccessController.doPrivileged(new PrivilegedExceptionAction() {
		@Override
		public Object run() throws RemoteException {
		    MAFAgentSystem_RMI rmi_message_sender = to_rmi_agentsystem(fMessageSender);

		    while (true) {
			try {
			    long r = fAgentSystem.receive_future_message(fAgentName, fMsg, rmi_message_sender);

			    return new Long(r);
			} catch (ConnectException ex) {
			    MAFAgentSystem_RMIClient.this.rebind(ex);
			} catch (UnmarshalException ex) {
			    MAFAgentSystem_RMIClient.this.rebind(ex);
			} catch (MarshalException ex) {
			    MAFAgentSystem_RMIClient.this.rebind(ex);
			}
		    }
		}
	    });

	    return result.longValue();
	} catch (PrivilegedActionException ex) {
	    RemoteException ee = (RemoteException) ex.getException();

	    if (ee.detail instanceof AgentNotFound) {
		throw (AgentNotFound) ee.detail;
	    } else if (ee.detail instanceof MAFExtendedException) {
		throw (MAFExtendedException) ee.detail;
	    } else if (ee.detail instanceof RuntimeException) {
		throw (RuntimeException) ee.detail;
	    } else {
		ex.printStackTrace();
		throw new RuntimeException(ee.getMessage());
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	    return -1;
	}
    }

    @Override
    public void receive_future_reply(long return_id, byte[] reply)
    throws EntryNotFound,
    MAFExtendedException {
	try {
	    final MAFAgentSystem_RMI fAgentSystem = this._agent_system;
	    final long fReturnID = return_id;
	    final byte[] fReply = reply;

	    AccessController.doPrivileged(new PrivilegedExceptionAction() {
		@Override
		public Object run() throws RemoteException {
		    while (true) {
			try {
			    fAgentSystem.receive_future_reply(fReturnID, fReply);
			    return null;
			} catch (ConnectException ex) {
			    MAFAgentSystem_RMIClient.this.rebind(ex);
			} catch (UnmarshalException ex) {
			    MAFAgentSystem_RMIClient.this.rebind(ex);
			} catch (MarshalException ex) {
			    MAFAgentSystem_RMIClient.this.rebind(ex);
			}
		    }
		}
	    });
	} catch (PrivilegedActionException ex) {
	    RemoteException ee = (RemoteException) ex.getException();

	    if (ee.detail instanceof EntryNotFound) {
		throw (EntryNotFound) ee.detail;
	    } else if (ee.detail instanceof MAFExtendedException) {
		throw (MAFExtendedException) ee.detail;
	    } else if (ee.detail instanceof RuntimeException) {
		throw (RuntimeException) ee.detail;
	    } else {
		ex.printStackTrace();
		throw new RuntimeException(ee.getMessage());
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	    return;
	}
    }

    @Override
    public byte[] receive_message(Name agent_name, byte[] msg)
    throws AgentNotFound,
    NotHandled,
    MessageEx,
    MAFExtendedException {
	try {
	    final MAFAgentSystem_RMI fAgentSystem = this._agent_system;
	    final Name fAgentName = agent_name;
	    final byte[] fMsg = msg;

	    return (byte[]) AccessController.doPrivileged(new PrivilegedExceptionAction() {
		@Override
		public Object run() throws RemoteException {
		    while (true) {
			try {
			    return fAgentSystem.receive_message(fAgentName, fMsg);
			} catch (ConnectException ex) {
			    MAFAgentSystem_RMIClient.this.rebind(ex);
			} catch (UnmarshalException ex) {
			    MAFAgentSystem_RMIClient.this.rebind(ex);
			} catch (MarshalException ex) {
			    MAFAgentSystem_RMIClient.this.rebind(ex);
			}
		    }
		}
	    });
	} catch (PrivilegedActionException ex) {
	    RemoteException ee = (RemoteException) ex.getException();

	    if (ee.detail instanceof AgentNotFound) {
		throw (AgentNotFound) ee.detail;
	    } else if (ee.detail instanceof NotHandled) {
		throw (NotHandled) ee.detail;
	    } else if (ee.detail instanceof MessageEx) {
		throw (MessageEx) ee.detail;
	    } else if (ee.detail instanceof MAFExtendedException) {
		throw (MAFExtendedException) ee.detail;
	    } else if (ee.detail instanceof RuntimeException) {
		throw (RuntimeException) ee.detail;
	    } else {
		ex.printStackTrace();
		throw new RuntimeException(ee.getMessage());
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	    return null;
	}
    }

    @Override
    public void receive_oneway_message(Name agent_name, byte[] msg)
    throws AgentNotFound,
    MAFExtendedException {
	try {
	    final MAFAgentSystem_RMI fAgentSystem = this._agent_system;
	    final Name fAgentName = agent_name;
	    final byte[] fMsg = msg;

	    AccessController.doPrivileged(new PrivilegedExceptionAction() {
		@Override
		public Object run() throws RemoteException {
		    while (true) {
			try {
			    fAgentSystem.receive_oneway_message(fAgentName, fMsg);
			    return null;
			} catch (ConnectException ex) {
			    MAFAgentSystem_RMIClient.this.rebind(ex);
			} catch (UnmarshalException ex) {
			    MAFAgentSystem_RMIClient.this.rebind(ex);
			} catch (MarshalException ex) {
			    MAFAgentSystem_RMIClient.this.rebind(ex);
			}
		    }
		}
	    });
	} catch (PrivilegedActionException ex) {
	    RemoteException ee = (RemoteException) ex.getException();

	    if (ee.detail instanceof AgentNotFound) {
		throw (AgentNotFound) ee.detail;
	    } else if (ee.detail instanceof MAFExtendedException) {
		throw (MAFExtendedException) ee.detail;
	    } else if (ee.detail instanceof RuntimeException) {
		throw (RuntimeException) ee.detail;
	    } else {
		ex.printStackTrace();
		throw new RuntimeException(ee.getMessage());
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	    return;
	}
    }

    @Override
    public void resume_agent(Name agent_name)
    throws AgentNotFound,
    ResumeFailed,
    AgentIsRunning {
	try {
	    final MAFAgentSystem_RMI fAgentSystem = this._agent_system;
	    final Name fAgentName = agent_name;

	    AccessController.doPrivileged(new PrivilegedExceptionAction() {
		@Override
		public Object run() throws RemoteException {
		    while (true) {
			try {
			    fAgentSystem.resume_agent(fAgentName);
			    return null;
			} catch (ConnectException ex) {
			    MAFAgentSystem_RMIClient.this.rebind(ex);
			} catch (UnmarshalException ex) {
			    MAFAgentSystem_RMIClient.this.rebind(ex);
			} catch (MarshalException ex) {
			    MAFAgentSystem_RMIClient.this.rebind(ex);
			}
		    }
		}
	    });
	} catch (PrivilegedActionException ex) {
	    RemoteException ee = (RemoteException) ex.getException();

	    if (ee.detail instanceof AgentNotFound) {
		throw (AgentNotFound) ee.detail;
	    } else if (ee.detail instanceof ResumeFailed) {
		throw (ResumeFailed) ee.detail;
	    } else if (ee.detail instanceof AgentIsRunning) {
		throw (AgentIsRunning) ee.detail;
	    } else if (ee.detail instanceof RuntimeException) {
		throw (RuntimeException) ee.detail;
	    } else {
		ex.printStackTrace();
		throw new RuntimeException(ee.getMessage());
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	    return;
	}
    }

    @Override
    public byte[] retract_agent(Name agent_name)
    throws AgentNotFound,
    MAFExtendedException {
	try {
	    final MAFAgentSystem_RMI fAgentSystem = this._agent_system;
	    final Name fAgentName = agent_name;

	    return (byte[]) AccessController.doPrivileged(new PrivilegedExceptionAction() {
		@Override
		public Object run() throws RemoteException {
		    while (true) {
			try {
			    return fAgentSystem.retract_agent(fAgentName);
			} catch (ConnectException ex) {
			    MAFAgentSystem_RMIClient.this.rebind(ex);
			} catch (UnmarshalException ex) {
			    MAFAgentSystem_RMIClient.this.rebind(ex);
			} catch (MarshalException ex) {
			    MAFAgentSystem_RMIClient.this.rebind(ex);
			}
		    }
		}
	    });
	} catch (PrivilegedActionException ex) {
	    RemoteException ee = (RemoteException) ex.getException();

	    if (ee.detail instanceof AgentNotFound) {
		throw (AgentNotFound) ee.detail;
	    } else if (ee.detail instanceof MAFExtendedException) {
		throw (MAFExtendedException) ee.detail;
	    } else if (ee.detail instanceof RuntimeException) {
		throw (RuntimeException) ee.detail;
	    } else {
		ex.printStackTrace();
		throw new RuntimeException(ex.getMessage());
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	    return null;
	}
    }

    @Override
    public void setAddress(String name) {
	throw new NoSuchMethodError();
    }

    @Override
    public void suspend_agent(Name agent_name)
    throws AgentNotFound,
    SuspendFailed,
    AgentIsSuspended {
	try {
	    final MAFAgentSystem_RMI fAgentSystem = this._agent_system;
	    final Name fAgentName = agent_name;

	    AccessController.doPrivileged(new PrivilegedExceptionAction() {
		@Override
		public Object run() throws RemoteException {
		    while (true) {
			try {
			    fAgentSystem.suspend_agent(fAgentName);
			    return null;
			} catch (ConnectException ex) {
			    MAFAgentSystem_RMIClient.this.rebind(ex);
			} catch (UnmarshalException ex) {
			    MAFAgentSystem_RMIClient.this.rebind(ex);
			} catch (MarshalException ex) {
			    MAFAgentSystem_RMIClient.this.rebind(ex);
			}
		    }
		}
	    });
	} catch (PrivilegedActionException ex) {
	    RemoteException ee = (RemoteException) ex.getException();

	    if (ee.detail instanceof AgentNotFound) {
		throw (AgentNotFound) ee.detail;
	    } else if (ee.detail instanceof SuspendFailed) {
		throw (SuspendFailed) ee.detail;
	    } else if (ee.detail instanceof AgentIsSuspended) {
		throw (AgentIsSuspended) ee.detail;
	    } else if (ee.detail instanceof RuntimeException) {
		throw (RuntimeException) ee.detail;
	    } else {
		ex.printStackTrace();
		throw new RuntimeException(ee.getMessage());
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	    return;
	}
    }

    @Override
    public void terminate_agent(Name agent_name)
    throws AgentNotFound,
    TerminateFailed {
	try {
	    final MAFAgentSystem_RMI fAgentSystem = this._agent_system;
	    final Name fAgentName = agent_name;

	    AccessController.doPrivileged(new PrivilegedExceptionAction() {
		@Override
		public Object run() throws RemoteException {
		    while (true) {
			try {
			    fAgentSystem.terminate_agent(fAgentName);
			    return null;
			} catch (ConnectException ex) {
			    MAFAgentSystem_RMIClient.this.rebind(ex);
			} catch (UnmarshalException ex) {
			    MAFAgentSystem_RMIClient.this.rebind(ex);
			} catch (MarshalException ex) {
			    MAFAgentSystem_RMIClient.this.rebind(ex);
			}
		    }
		}
	    });
	} catch (PrivilegedActionException ex) {
	    RemoteException ee = (RemoteException) ex.getException();

	    if (ee.detail instanceof AgentNotFound) {
		throw (AgentNotFound) ee.detail;
	    } else if (ee.detail instanceof TerminateFailed) {
		throw (TerminateFailed) ee.detail;
	    } else if (ee.detail instanceof RuntimeException) {
		throw (RuntimeException) ee.detail;
	    } else {
		ex.printStackTrace();
		throw new RuntimeException(ee.getMessage());
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	    return;
	}
    }

    static MAFAgentSystem_RMI to_rmi_agentsystem(MAFAgentSystem __maf) {
	if (__maf instanceof MAFAgentSystem_RMIClient) {
	    return ((MAFAgentSystem_RMIClient) __maf)._agent_system;
	} else {
	    return MAFAgentSystem_RMIImpl.find_rmi_agentsystem(__maf);
	}
    }
}
