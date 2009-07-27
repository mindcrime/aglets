package net.sourceforge.aglets.rolex;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * @author Sandro Cremonini
 * @version 1.0
 */

import BlackCat.core.role.RoleSupport;
import BlackCat.core.agentLibrary.Agent;
import BlackCat.core.agentLibrary.SimpleAgent;
import BlackCat.core.role.database.RoleLocator;
import BlackCat.core.role.GenericRoleDescription;
//import BlackCat.core.role.exception.*;
import BlackCat.core.role.LoaderWarnings;
import BlackCat.core.event.SystemEventListener;
import BlackCat.core.middleware.times.Chronograph;
import BlackCat.GUI.MagicPrintStream;
import java.util.Vector;
import java.util.Stack;
import BlackCat.core.role.Warning;
import java.io.*;
import java.net.URL;
import java.security.SecureClassLoader;
import java.lang.SecurityException;
import java.security.AccessController;
import java.security.Permission;
import java.security.Permissions;
import java.security.PermissionCollection;
import java.lang.reflect.ReflectPermission;
import java.lang.reflect.*;
import java.lang.reflect.Modifier;
import java.util.Enumeration;
import javassist.*;
import javassist.bytecode.*;
import BlackCat.core.role.exception.IncompatibilityException;
import BlackCat.core.role.exception.RoleException;


public class RoleLoader extends SecureClassLoader implements RolexLoader {

    /**
     * debug flag
     */
    protected boolean debug=true;

    /**
     * The chronograph, used to register the time.
     */
    protected Chronograph crono=Chronograph.getInstance();

    /**
     * An indentity flag, used only to identify this class loader on screen
     * message.
     */
    public int myNumber=0;

    /**
     * Permissions to execute. Put here all the permission your loader
     * must has,except the reflect permission and loader permission that
     * are setted automatically.
     */
    private Permissions permissions=null;

    /**
     * The role locator.
     * Contact the locator to check the role description and get the
     * urls classpathes.
     */
    private RoleLocator locator=null;




    /**
     * Flag to inidicate if the loader must add roles or remove roles.
     */
    private boolean add=true;


    /**
     * The inheritance stack. It's used to know what role must be loaded at
     * what level of the inheritance chain.
     * This stack stores a string array in which the first name is the name
     * of the class to load and the others the names of the role to be
     * loaded.
     */
    private Stack roleStack=null;


    /**
     * A vector to store the class already copied. Put here all the class names
     * that has been already copied (methods and members).
     */
    private Vector alreadyCopied=null;


    /**
     * Current superclass. Used during level regenaration.
     */
    private CtClass superclass=null;


    /**
     * Warnings. This vector stores any warning (not errors) that could
     * make role unusable. The warning are stored according to the warning
     * class and the vector stores three values:<BR>
     * warning type - class on which the warning is - class on which the warning
     * is by<BR>
     *
     */
    private Vector warnings=null;


    /**
     * Set the permissions to run.
     * In this implementation automatically set the reflectpermissions
     * and the loader permissions.<B> if there is not a createclassloader
     * permission in the class that load this loader a security exception
     * is thrown on the constructor. Can't check that permission here.
     */
    protected final void addBasePermissions()
    {
        /* check if the permissions collection is null */
        if(this.permissions==null)
        {
            if(debug)
            { msg("The permission collection is null, create it "); }

            /* create a new permission collection */
            this.permissions=new Permissions();

            if(debug)
            {   msg("Permissions collection created");  }
        }

        /* now add the permissions to the collection */
        if(debug)
        { msg("Adding reflectpermission to the collection");    }

        if(this.permissions.isReadOnly()==false)
        {
            this.permissions.add(new ReflectPermission("suppressAccessChecks"));
        }


    }




    /**
     * Add a generic permission to the collection.
     * The collection is etherogenous, so simply set the permission.
     * @param perm the permission to add.
     */
    protected final void addPermission(Permission perm)
    {
        /* check params */
        if(perm==null)
        {
            return;
        }

        /* check if the permission collection is null, then create it */
        if(this.permissions==null)
        {
            if(debug)
            { msg("Create a new collection ");  }

            /* create the permission */
            this.permissions=new Permissions();

            /* this permissions collection is now empty, add the
            base permissions */
            this.addBasePermissions();
        }

        /* now add the permission to the collection */
        if(debug)
        {   msg("adding permission "+perm.toString()+" to the collection");
            msg("Permission informations:");
            msg("name" +perm.getName());
            msg("actions "+perm.getActions());
        }

        /* adding the permission */
        if(this.permissions.isReadOnly()==false)
        {
            this.permissions.add(perm);
        }

        if(debug)
        {   msg("permission "+perm.toString()+" added");    }
    }

    /**
     * Method to check permissions.
     * This method check if the loader has all the permissions
     * to execute, in particular the loading permissions and the reflect
     * permissions to set private members during reloading.
     * The security exceptions are caught and don't reported out of this
     * method.
     * The method test automatically the adjuncts permissions is there's one.
     * @return true if all permissions ok, false if it could not proceed.
     */
    protected boolean checkRoleLoaderPermissions()
    {
        /* try to invoke the accesscontroller.check method for every
        permission setted in the collection and then catch the security
        exceptions (if there are) */
        if(this.permissions==null)
        {
            /* no permissions defined, I've them */
            if(debug)
            {   msg("No permissions to check, the collection is empty");    }

            return true;
        }

        /* get the permissions enumeration */
        Enumeration enumer=this.permissions.elements();

        Permission toCheck=null;

        /* check the permissions */
        try
        {
            while(enumer.hasMoreElements())
            {
                /* call the static method checkPermission to check if I've got
                the permission */
                toCheck=(Permission)enumer.nextElement();
                AccessController.checkPermission(toCheck);
            }
        }
        catch(SecurityException exc)
        {
            /* I've not all the permissions, return false */
            return false;
        }

        /* if here I've got all the permissions */
        return true;

    }


    /**
     * Method to set memebers in the loaded class.
     * This method is very powerful, it check between two objects all the
     * common members and then copy the value of the source into the
     * destination. It required (but not need at all) the reflectpermission
     * because some members can be private or protected.
     * @param src the src object fom which extract values
     * @param srcClass the source class object that allow to make
     * cast operation
     * @param dest the destination object into copy values
     * @param destClass the dest class object that allow casting
     * @param fromRole true if you want to copy role initialization.
     * @exception IllegalArgumentException is thrown if you try to access to
     * a member and you can't. This don't means that is a problem of reflerct
     * permissions, this could mean that the class object (java.lang.Class) are
     * not the same.
     * @exception IllegalAccessException thrown if you can't access to the
     * member
     * @deprecated Use copyMembersFormRole instead.
     */
    protected final boolean copyMembers(Object src,Class srcClass,
                                        Object dest,Class destClass,boolean fromRole)
        throws IllegalArgumentException,IllegalAccessException
    {
        /* check params */
        if(src==null || srcClass==null || dest==null || destClass==null)
        {
            /* can't proceed */
            return false;
        }




        /* ATTENTION:
        only copy on exactly same class objects */
        if(destClass.equals(srcClass)==false && fromRole==false)
        {
            return false;
        }

        /* make the loop for the copy .
        In the loop do these steps:
        1) get the value
        2) make the dest field accesible
        3) copy the field
        4) make the dest unaccesible */

        /* get the fields */
        Field[] srcFields,destFields;
        srcFields=srcClass.getDeclaredFields();
        destFields=destClass.getDeclaredFields();

        if(destFields==null || srcFields==null)
        {
            return false;
        }

        boolean originalAccessType=true;    /* used to store the original
                                            access type of the fields */

        for(int i=0;i<srcFields.length;i++)
        {

            /*check if the member name (a variable must has an unique name)
            is the same, then and only then copy its value */
            for(int j=0;j<destFields.length;j++)
            {
                if(srcFields[i].getName().equals(destFields[j].getName()))
                {
                    /* store the access type */
                    originalAccessType=destFields[j].isAccessible();

                    /* make the dest field accesible */
                    destFields[j].setAccessible(true);

                    /* make accesible the src member */
                    srcFields[i].setAccessible(true);

                    if(debug)
                    {
                        msg("Found the member "+destFields[j].getName()+" ("+this.modifierTypeToString(destFields[i])+")");
                        //msg("The value of the src field is "+srcFields[i].get(src).toString());

                    }


                    /* copy the field */
                    /* to copy the fields simply transfer it from src to dest,
                    note that the get method automatically wrapped the primitive
                    members, so the value passed to the set method is right. */

                    /* Attention:
                    copying final members causes an IllegalAccessException!!!!! */
                    if(!Modifier.isFinal(destFields[j].getModifiers()) &&
                        !Modifier.isTransient(destFields[j].getModifiers()) &&
                        !Modifier.isVolatile(destFields[j].getModifiers()) &&
                        !Modifier.isStatic(destFields[j].getModifiers()))
                    {
                        destFields[j].set(dest,srcFields[i].get(src));

                        if(debug)
                        {   msg("copy done");    }

                    }


                    /* now set the member at the accesible type orignal */
                    destFields[j].setAccessible(originalAccessType);
                    srcFields[i].setAccessible(originalAccessType);
                }
            }

        }

        /** al done */
        return true;


    }

    /**
     * Method for copy fields values between two objects.
     * Originally designed to copy only roles fields <B>this method should be
     * used for every copy-operation because it accepts different Class types
     * </B>, instead copyMembers no.
     * The method do this steps:
     * <BR>
     * 1) checks if the members on the two objects have the same name. Infact
     * a variable is identified by name (that must be unique), so no other tests
     * are neeeded to found two variables correspondebece.<BR>
     * 2) store the original access type (public,private,ecc.) for the two
     * variables (the access type should be the same in the two object, in this
     * implementation but here every access type is stored so they could be
     * different).<BR>
     * 3) set the members accessibles (here could be needed the
     * suppressAccessChecks permission).<BR>
     * 4) copy the field value<BR>
     * 5) report the access type to the original value.<BR>
     * <BR>
     * <B>
     * Note that the class type for objects is specififed like a parameter and
     * not calculated at runtime with getClass() method. This is much powerful
     * because it allow you to threate an object like a type that you could
     * specify</B>.<BR>
     * @param src the object instance that has the values to be copied
     * @param srcClass the class type of the source
     * @param dest the object instance into which copy the values
     * @param destClass the class type of the destination object.
     * @param elevateBoth true if you want copy members from agent, false
     * if you want to copy members from role.
     * @exception IllegalArgumentException is thrown if you try to access to
     * a member and you can't. This don't means that is a problem of reflerct
     * permissions, this could mean that the class object (java.lang.Class) are
     * not the same.
     * @exception IllegalAccessException thrown if you can't access to the
     * member
     */
    protected final boolean copyMembersFromRole(Object src,Class srcClass,
                                                Object dest,Class destClass,
                                                boolean elevateBoth)
        throws IllegalArgumentException,IllegalAccessException
    {
        /* check params */
        if(src==null || srcClass==null || dest==null || destClass==null)
        {
            /* can't proceed */
            return false;
        }


        do
        {

            msg("Le classi sono src="+srcClass.getName()+" dest="+destClass.getName());
            /* make the loop for the copy .
            In the loop do these steps:
            1) get the value
            2) make the dest field accesible
            3) copy the field
            4) make the dest unaccesible */

            /* get the fields */
            Field[] srcFields,destFields;
            srcFields=srcClass.getDeclaredFields();
            destFields=destClass.getDeclaredFields();

            if(destFields==null || srcFields==null)
            {
                return false;
            }

            boolean originalSrcAccessType=true;    /* used to store the original
                                                        access type of the fields */
            boolean originalDestAccessType=true;

            for(int i=0;i<srcFields.length;i++)
            {
                for(int j=0;j<destFields.length;j++)
                {
                    if(destFields[j].getName().equals(srcFields[i].getName()) &&
                        destFields[j].getType().equals(srcFields[i].getType()))
                    {
                        /* store the access type */
                        originalSrcAccessType=srcFields[i].isAccessible();
                        originalDestAccessType=destFields[j].isAccessible();

                        /* make the dest field accesible */
                        destFields[j].setAccessible(true);

                        /* make accesible the src member */
                        srcFields[i].setAccessible(true);

                        if(debug)
                        {
                            msg("Found the member "+destFields[j].getName()+" ("+this.modifierTypeToString(destFields[i])+")");
                        }


                        /* copy the field */
                        /* to cpy the fields simply transfer it from src to dest,
                        note that the get method automatically wrapped the primitive
                        members, so the value passed to the set method is right. */

                        /* Attention:
                        copying final members causes an IllegalAccessException!!!!! */
                        if(!Modifier.isFinal(destFields[j].getModifiers()) &&
                            !Modifier.isTransient(destFields[j].getModifiers()) &&
                            !Modifier.isVolatile(destFields[j].getModifiers()) &&
                            !Modifier.isStatic(destFields[j].getModifiers()))
                        {
                            destFields[j].set(dest,srcFields[i].get(src));

                            if(debug)
                            {   msg("copy done");    }

                        }


                        /* now set the member at the accesible type orignal */
                        destFields[j].setAccessible(originalDestAccessType);
                        srcFields[i].setAccessible(originalSrcAccessType);
                    }
                }

            }


            /* get the superclass */
            srcClass=srcClass.getSuperclass();

            /* if I'm copying members from an agent I must elevate my
            superclass */
            if(elevateBoth==true)
            {
                destClass=destClass.getSuperclass();
            }
        }
        while( (srcClass!=null && destClass!=null) &&
                (srcClass.equals(Object.class)==false ||
                destClass.equals(Object.class)==false));

        /** al done */
        return true;


    }










    /**
     * Method to check agent.
     * Before manipulate the agent check if it's right agent. Put in this
     * method the rules for the agent. In this implementation rules are:
     * <BR>
     * 1) the agent can't has static members (field or method) because the
     * agent is going to be modified, so there can be incoerence between the
     * original class and the running (modified) agent.
     * <BR>
     * 2) the agent should be a subclass of RoleSupport. There are there correct
     * subclassing chains:
     * <BR>
     * RoleSupport->Agent->SimpleAgent-><I>myagent</I>
     * <BR>
     * RoleSupport->Agent-><I>myagent</I>
     * <BR>
     * RoleSupport-><I>myagent</I><BR>
     * Assume that Agent,SimpleAgent and RoleSupport are ok for the agent,
     * but no static members are allowed.
     * @param agent the RolexAgent to check
     * @return true if the agent is ok for the defined rules, false if no
     */
    protected boolean checkAgent(Class agentClass)
    {

        /* little check */
        if(agentClass==null)
        {
            return false;
        }


        /* now check what type the agent is */
        if(agentClass.equals(SimpleAgent.class) )
        {
            /* the agent is a SimpleAgent, ok if no static members */
            return true;
        }
        else
        if(agentClass.equals(Agent.class) )
        {
            /* agent is a Agent, ok*/
            return true;
        }
        else
        if(agentClass.equals(RoleSupport.class) )
        {
            /* agent is a rolesupport, ok */
            return true;
        }
        else
        {
            /* agent is probably a subclass of simpleagent,rolesupport,
            or agent, check what is its superclass */
            agentClass=agentClass.getSuperclass();
            if(this.checkAgent(agentClass)==true )
                {
                    return true;
                }
                else
                {
                    return false;
                }

        }
    }



    /**
     * Method to check RoleSupport presence.
     * The method force a casting on the agent, if a ClassCastExcption is
     * thrown then the agent has no role support.
     * @param agent the agent to check (instance of the agent!!)
     * @return true if the agent has rolesupport, false if not.
     */
    protected final boolean checkRoleSupport(RolexAgent agent)
    {
        /* check params */
        if(agent==null)
        {
            return false;
        }

        /* try to cast the agent, if exception thrown then the agent has
        no role support. */
        try
        {
            RoleSupport r=(RoleSupport)agent;

            /* if here all right */
            r=null;
            return true;
        }
        catch(ClassCastException e)
        {
            return false;
        }


    }


    /**
     * Method to check the security access for the class loader.
     * This method check if every class on the stack level is accesible.
     * @exception SecurityException is thrown if one (or more) permissions
     * are denied.
     */
    protected void checkSecurityPermission()
        throws SecurityException
    {
        /* check if the security manager is on */
        SecurityManager sm=System.getSecurityManager();

        if(sm==null)
        {
            return;
        }


        /* clone the stack */
        Stack myStack=this.cloneRoleStack();

        /* check if the stack is null */
        if(myStack==null)
        {
            return;
        }

        /* now extract every class name from the stack and check it */
        String names[]=null;

        while(this.checkStack())
        {
            names=this.popFromStack();

            /* check every name */
            for(int i=0;i<names.length;i++)
            {
                if(names[i]!=null)
                {
                    /* check package access */
                    int k=names[i].lastIndexOf(".");
                    if(k!= -1)
                    {
                        /* check package access */
                        sm.checkPackageAccess(names[i].substring(0,k));

                        /* check package definition */
                        sm.checkPackageDefinition(names[i].substring(0,k));
                    }
                }
            }

        }

        /* recovery the stack */
        this.setRoleStack(myStack);



    }



    /**
     * Method to get the role implementation name.
     * @param role the description for the role
     * @return the string that represents the qualified class name
     */
//    protected String getRoleImplementationClass(Role role)
//    {
//        /* check if the repository is on */
//        if(this.locator!=null)
//        {
//            /* ask the locator the implementation name */
//            return this.locator.getRoleClass(role);
//
//        }
//
//        /* if here I have no locator */
//       return null;
//    }


    /**
     * Method to get the name of multiple roles implementations.
     * This method contact the locator to get multiple role names.
     * @return a vector of string with the roles names
     */
//    protected String[] getMultipleRoleImplementations(Role roles[])
//    {
//        /* check if the repository is on */
//        if(this.locator!=null)
//        {
//            /* create a new vector of strings */
//            String ret[]=new String[roles.length];
//
//           for(int i=0;i<ret.length;i++)
//            {
//                /* ask the locator the implementation name */
//
//                 ret[i]=this.locator.getRoleClass(roles[i]);
//            }
//            return ret;
//
//       }
//
//       /* if here I have no locator */
//        return null;
//    }


    /**
     * Method to get a multiple role interfaces names.
     * This method contact the locator to get the names.
     * @return a vector with the qualified names,null if can't contact the
     * locator.
     */
//   protected String[] getMultipleRoleInterfaces(Role roles[] )
//    {
//        /* check if the repository is on */
//        if(this.locator!=null)
//        {
//            /* create a new vector of strings */
//            String ret[]=new String[roles.length];
//
//            for(int i=0;i<ret.length;i++)
//            {
//                /* ask the locator the implementation name */
//
//                 ret[i]=this.locator.getRoleInterface(roles[i]);
//            }
//            return ret;
//
//        }
//
//        /* if here I have no locator */
//        return null;
//    }


    /**
     * Method to get a role interface.
     * @param role the role description
     * @return the interface name,null if can't contact the locator.
     */
//    protected String getRoleInterface(Role role)
//    {
//        /* check if the repository is on */
//        if(this.locator!=null)
//        {
//            /* ask the locator the implementation name */
//           return this.locator.getRoleInterface(role);
//
//        }
//
//        /* if here I have no locator */
//        return null;
//    }

    /**
     * Method to merge two ctclass vectors in one,without duplicates.
     * This method merge two vector of CtClass objects in one that has no
     * duplicates.
     * @param v1 the first vector
     * @param v2 the second vector
     * @return a single vector merged.
     */
    protected final CtClass[] mergeVector(CtClass[] v1,CtClass[] v2)
    {
        /* check params */
        if(v1==null && v2!=null)
        {
            return v2;
        }
        else
        if(v2==null && v2!=null)
        {
            return v1;
        }
        else
        if(v1==null && v2==null)
        {
            return null;
        }


        /* define the max length of the current vector */
        int max=(v1.length>v2.length) ? v1.length : v2.length;

        /* if here v1 and v2 are valid */
        Vector tmp=new Vector(max);

        /* copy whole v1 into temp vector */
        for(int i=0;i<v1.length;i++)
        {

            tmp.add((CtClass)v1[i]);
        }


        /* now add the non duplicated v2 entries */


        for(int i=0;i<v2.length;i++)
        {

            if(!tmp.contains((CtClass)v2[i]))
            {
                tmp.add((CtClass)v2[i]);
            }

        }

        /* now construct return vector */
        CtClass ret[]=new CtClass[tmp.size()];

        /* fill the array */
        for(int i=0;i<ret.length;i++)
        {
            ret[i]=(CtClass)tmp.get(i);
        }

        /* all done */
        return ret;

    }

    /**
     * Method to remove all the roles from an agent.
     * This method is used to remove all the roles from an agent in only one
     * shot.
     * @param agent the agent to manipulate
     * @return the new agent without roles
     * @excetion RoleLoaderException thrown if can't remove the roles
     */
    protected final RolexAgent removeAllRolesFromAgent(RolexAgent agent)
        throws RoleLoaderException,UnusableRoleLoaderException
    {
        /* check param */
        if(agent==null)
        {
            msg("The agent is null, nothing to do!");
            return null;
        }

        /* to release all roles I must only reload the agent
        without all roles, that is without all interfaces */
        try
        {
            /* I must recompute the void stack inheritance chain */
            msg("Calculating inheritance stack...");
            //this.(agent.getClass(),null);
            try
            {
            this.computeInheritanceChain(Class.forName(agent.getClass().getName()),null);
            }
            catch(Exception e)
            {
            }
            msg("inheritance stack calculated!");

            this.stampa_stack();



          /* create an array to store the loaded classes */
          Class chain[]=new Class[this.getRoleStackSize()];
          int level=0;

          /* stores the original stack before use it */
          Stack originalStack=this.cloneRoleStack();

          /* now I must create every level and define it */
          while(this.checkStack()==true)
          {
                chain[level]=this.generateLevel();
                level++;

          }

          /* recovery the stack */
          this.setRoleStack(originalStack);

          /* now the agent is on the last class */
          RolexAgent newAgent=(RolexAgent)chain[(level-1)].newInstance();



          /* now I must copy all fields values. To do it use the
          copyMembersValue copying every value between the original object
          (original agent) and then from every role */


          /* get the original agent class */
          Class oldAgentClass=agent.getClass();

          /* get the new agent class, stored in chain[level--] */
          Class newAgentClass=chain[(level-1)];

          level-=2;

          /* now copy all the mebers recursively */
          while(newAgentClass!=null && oldAgentClass!=null /*&& level>0*/)
          {
            msg("\n\n\n\n\tle classi sono "+newAgentClass.getName()+" "+oldAgentClass.getName()+"\n\n\n");
            msg("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n\n");
            int mm;
            mm=this.copyMembersValue(agent,oldAgentClass,newAgent,newAgentClass);

            msg("sono stati copiati "+mm+" variabili");

            /* now go up to the superclass */
            //newAgentClass=chain[level];
            level--;

            newAgentClass=newAgentClass.getSuperclass();
            oldAgentClass=oldAgentClass.getSuperclass();

          }

          return newAgent;


        }
        catch(IllegalAccessException e)
        {
            /* can't access to the class package or to a member into the object
            */
            throw new RoleLoaderException("Can't access to a class member or to the package");
        }
        catch(InstantiationException e)
        {
            /* can't create the role or the agent */
            throw new RoleLoaderException("can't create the agent or the role ");
        }
/*	catch(ClassNotFoundException e)
        {
            throw new RoleLoaderException("can't found the role class");
        }
*/	catch(IOException e)
        {
            throw new RoleLoaderException("can't write bytecode");
        }
        catch(CannotCompileException e)
        {
            throw new RoleLoaderException("Can't compile manipulated bytecode");
        }
        catch(NotFoundException e)
        {
            throw new RoleLoaderException("Class not found ");
        }

    }

    /**
     * Method to remove all roles.
     * @param agent the agent to manipulate
     * @exception RoleLoaderException if errors
     */
    public RolexAgent removeAllRoles(RolexAgent agent)
        throws RoleLoaderException,UnusableRoleLoaderException
    {
        return this.removeAllRolesFromAgent(agent);
    }


    /**
     * Method to append a classpath to a constant pool object.
     * Use this method before load class CtClass by the ClassPoll object.
     * The locator knows where find objects (that is classpath) but the
     * constantpoll could not know it, so request to the locator all
     * the classpath and add them to the pool.
     * @param pool the constant pool to modify
     */
    protected final void addClasspathToPoll(ClassPool pool)
        throws javassist.NotFoundException
    {
        /* check param */
        if(pool==null)
        {
            return;
        }


        /* check if the role locator is null */
        if(this.locator==null)
        {
            return;
        }

        /* extract from the locator every path and add to the pool */
        Vector path=this.locator.getClasspath();
        String s=null;

        /* now add the paths */
        if(path!=null)
        {
            for(int i=0;i<path.size();i++)
            {
                s=((URL)path.get(i)).toExternalForm();
                pool.appendClassPath((String)s);
            }
        }

    }



    /**
     * Method to add multiple roles to an agent.
     * @param agent the agent to manipulate
     * @param roleDescriptionthe description of the role to be added
     * @exception RoleException if can not compile
     * @exception IncompatiblityException if the roles are incompatibles
     * @return the class object manipulated
     */
    public RolexAgent addMultipleRoleToAgent(RolexAgent agent,Role roles[])
        throws RoleException,IncompatibilityException,UnusableRoleLoaderException, RoleLoaderException,
            IllegalArgumentException {

        /* check compatibility */
        this.checkCompatibility(roles);
        return (RolexAgent)this.addRole(agent,roles);
    }



    /**
     * Method to load multiple roles from the locator.
     * You should use this method to add to your agent multiple roles.
     * @param descriptions the generic role description to load
     * @param agent the agent to manipulate
     * @return a new instance of the agent
     */
    protected final RolexAgent addRole(RolexAgent agent,Role[] descriptions)
        throws RoleException,IncompatibilityException,UnusableRoleLoaderException, IllegalArgumentException,
            RoleLoaderException {

        /* check params */
        if(agent==null || descriptions==null || descriptions.length==0)
        {
            /* nothing to do */
            return agent;
        }



        /* check reflect and base permissions */
        if(this.checkRoleLoaderPermissions()==false)
        {
            return null;
        }


        /* now get the names of role and agent */
//        String agentName=agent.getClass().getName();
//        String[] interfacesName=this.getMultipleRoleInterfaces(descriptions);
//        String[] implementationsName=this.getMultipleRoleImplementations(descriptions);


        /* check if all ok */
//        if(agentName==null || interfacesName==null || interfacesName.length==0 ||
//            implementationsName==null || implementationsName.length==0)
//        {
//            return null;
//        }


        /* now try to manipulate bytecode */
        byte[] code=null;



          /* check loader permissions */
          this.checkSecurityPermission();

          /* reset the warning vector */
          this.warnings=null;

          /* compute the inheritance chain */
//          Class c1=Class.forName(agentName);
//          Class c2[]=new Class[implementationsName.length];

//          for(int i=0;i<c2.length;i++)
//          {
//            c2[i]=Class.forName(implementationsName[i]);
//          }

//          this.computeInheritanceChain(c1,c2);



          Class c1 = (Class) agent.getClass();
          Class c2[] = new Class[descriptions.length];

          for(int i=0; i<c2.length; i++)
             c2[i] = (Class) descriptions[i].getClass();


          return addRoleFromDescription(agent,c1,c2);

 }



    protected RolexAgent addRoleFromDescription(RolexAgent agent, Class c1, Class c2[]) throws
           RoleException,IncompatibilityException,UnusableRoleLoaderException, IllegalArgumentException,RoleLoaderException {


         if(agent==null || c1==null || c2.length==0)
         {
            /* nothing to do */
            return agent;
         }

         /* c_prova is a copy of c2, because c2 become null after computeInheritanceChain*/
         Class c_prova[] = new Class[c2.length];
         for(int i=0;i<c2.length;i++) {
            c_prova[i]=c2[i];
         }


          try {

          // store classes in the stack
          this.computeInheritanceChain(c1,c2);

          /* check the loader cache */
          this.checkRoleLoaderCache();



          /* create an array to store the loaded classes */
          Class chain[]=new Class[this.getRoleStackSize()];
          int level=0;

          /* stores the original stack before use it */
          Stack originalStack=this.cloneRoleStack();

          /* now I must create every level and define it */
          while(this.checkStack()==true)
          {
                chain[level]=this.generateLevel();

                level++;
          }

          /* recovery the stack */
          this.setRoleStack(originalStack);



          System.out.println("\nSTAMPA");

          /* now the agent is on the last class */
          System.out.println("Creazione classe "+chain[(level-1)]);
          //net.sourceforge.aglets.rolex.test.TestAgent.dumpAgent(chain[(level-1)]);
          RolexAgent newAgent=(RolexAgent)chain[(level-1)].newInstance();



          /* now I must copy all fields values. To do it use the
          copyMembersValue copying every value between the original object
          (original agent) and then from every role */


          /* get the original agent class */
          Class oldAgentClass=agent.getClass();

          /* get the new agent class, stored in chain[level--] */
          Class newAgentClass=chain[(level-1)];

          level-=2;




          /* now copy all the mebers recursively */
          while(newAgentClass!=null && oldAgentClass!=null /*&& level>0*/)
          {
            //System.out.println("\n\n\tCOPIA DEI MEMBRI FRA I LIVELLI " +newAgentClass.getName()+" <- "+oldAgentClass.getName()+"\n\n");
            //System.out.println("\tLEVEL ="+level);

            this.copyMembersValue(agent,oldAgentClass,newAgent,newAgentClass);

            /* now go up to the superclass */
            //newAgentClass=chain[level];
            level--;

            newAgentClass=newAgentClass.getSuperclass();
            oldAgentClass=oldAgentClass.getSuperclass();

          }



          /* Now I have the agent manipulated and I have copied every
          field value that the old agent and the new agent have both. I must
          copy values from every role now, and at every level. I know that the
          stack is already on because I've not destoied it. Use the stack
          to copy fileds. But the stack stores a string array, I must
          search for the agent on every level and than for every role.
          The agent is stored on the chain array and the roles into the
          string vector. */

          /* ATTENTION:
          i must do the copy from the last class to the base class, I must
          reverse the stack */
          this.reverseRoleStack();




          String rn[]=null;
          Object roleInstance=null;
          Class rClass=null;
          level=chain.length-1;

          while(this.checkStack())
          {
                rn=this.popFromStack();

                /* now create only instance of the role different
                from the agent */
                for(int i=0;i<rn.length;i++)
                {
                    if(rn[i]!=null && rn[i].equals(chain[level].getName())==false
                        && rn[i].equals((String)"java.labg.Object")==false)
                    {
                        /* this role is not used as an agent, create a new
                        instance of it */
                        rClass=Class.forName(rn[i]);
                        roleInstance=rClass.newInstance();
                        this.copyMembersValue(roleInstance,rClass,newAgent,chain[level]);
                    }
                }

                /* go up */
                level--;

          }



          /* release superclass */
          this.superclass=null;

          /* add interface-couple to agent records */
          for(int i=0;i<c_prova.length;i++) {
              Class roleInterface = this.getRoleInterface(c_prova[i]);
              newAgent.storeRoleInformation(roleInterface.getName(),c_prova[i].getName());
          }

          /* now the agent is complete */
          msg("Role added to the agent, returning it");
          return newAgent;
      }


        catch(IOException e)
        {
            /* can't write on pool */
            throw new RoleLoaderException("Error while loading on the pool:"+e.getMessage());
        }
        catch(NotFoundException e)
        {
            /* error loading classes */
            throw new RoleLoaderException("Error loading classes:\n"+e.getMessage());
        }
        catch(CannotCompileException e )
        {
            /* error during compiling */
            throw new RoleLoaderException("Error compiling pools:\n"+e.getMessage());
        }
        catch(IllegalAccessException e)
        {
            /* can't access to the class package or to a member into the object
            */
            throw new RoleLoaderException("Can't access to a class member or to the package");
        }
        catch(InstantiationException e)
        {
            /* can't create the role or the agent */
            throw new RoleLoaderException("can't create the agent or the role ");
        }
        catch(ClassNotFoundException e)
        {
            e.printStackTrace();
            throw new RoleLoaderException("can't found the role class");
        }
        catch(SecurityException e)
        {
            /* can't access to a package */
            throw new RoleLoaderException("Package access or definition denied");
        }

    }



protected Class getRoleInterface(Role roleObject){

    // check params
    if( roleObject == null ){
        return null;
    }

    Class interfaces[] = roleObject.getClass().getInterfaces();

    Class current = null;



    for(int i=0;i<interfaces.length;i++){
        current = interfaces[i];

        if( current!= null && current.equals(Role.class) ){
            return Role.class;
        }
        else
        if( current!=null && extendsRoleInterface(current)==true ){
            return current;
        }


    }

    return null;
}


protected Class getRoleInterface(Class roleObject){

    // check params
    if( roleObject == null ){
        return null;
    }

    Class interfaces[] = roleObject.getInterfaces();

    Class current = null;



    for(int i=0;i<interfaces.length;i++){
        current = interfaces[i];

        if( current!= null && current.equals(Role.class) ){
            return Role.class;
        }
        else
        if( current!=null && extendsRoleInterface(current)==true ){
            return current;
        }


    }

    return null;
}



protected final boolean extendsRoleInterface(Class interf){
    Class interfaces[] = interf.getInterfaces();

    if( interfaces == null )
        return false;



    for(int i=0;i<interfaces.length;i++){
        if( interfaces[i].equals(Role.class) )
            return true;

        if( extendsRoleInterface(interfaces[i]) == true ){
            return true;
        }
    }

    return false;
}


    /**
     * Method to add a single role. You should use this method
     * to add a role to an agent that has already some roles.
     * @param agent the agent you want to manipulate
     * @role the role you want to add
     */
//    public RolexAgent addAnotherRoleToAgent(RolexAgent agent, Role role)
//            throws RoleException,IncompatibilityException,UnusableRoleLoaderException
//    {
//        /* checks param */
//        if(agent==null || role==null)
//        {
//            return (RolexAgent)agent;
//        }
//
//        /* to add a role I must reload the agent adding all the roles it has
//        and the role added */
//
//        /* obtain the roles already added: a role is an interface that
//          extends GenericRole */
//        Class interfaces[]=agent.getClass().getInterfaces();
//        Vector roles=new Vector(interfaces.length);
//
//        /* store every role interface*/
//        for(int i=0;i<interfaces.length;i++)
//        {
//
//            /* if the interface is a role interface store it */
//            if(this.isRoleInterface(interfaces[i],false)==true)
//            {
//                roles.add((Class)interfaces[i]);
//                msg("role interface found "+interfaces[i].getName());
//            }
//        }
//
//
//        /* Now I have the role interfaces to add, the agent, the new role
//        but I need the role implementations for previous interfaces. */
//        Role desc[]=new Role[roles.size()+1];
//        desc[0]=role;
//
//        String name=null;
//
//        for(int i=1;i<desc.length;i++)
//        {
//            name=((Class)roles.get((i-1))).getName();
//            msg(name);
//            desc[i]=this.getDescriptorByName(name);
//            msg(desc[i].getName());
//        }

        /*check for compatible roles */
//        this.checkCompatibility(desc);

        /* now i'm ready to manipulate agent */
        /* insert debug informations into the time table */
//        String s=desc[0].getName();

//        for(int i=1;i<desc.length;i++)
//        {
//          s+=","+desc[i].getName();
//        }
//        crono.addTimeEvent(100,System.currentTimeMillis(),"Role Loader starts adding role(s): "+s);

        /* do manipulation */
//        return (RolexAgent)this.addRole(agent,desc);
//    }


    /**
     * Method to remove a role from an agent.
     * This method search for every role that the agent has at this time
     * and remove only the specififed role.
     * @param agent the agent to manipulate
     * @param role the description of the role
     * @return the new agent without the specified role
     * @exception IncompatibilityException if a compatibility error occurs
     * @exception RoleLoaderException if loader internal error
     * @exception RoleException if role is bad
     */
    public RolexAgent removeSingleRoleFromAgent(RolexAgent agent, Role role)
        throws RoleException,IncompatibilityException,RoleLoaderException,UnusableRoleLoaderException
    {
        /* check params */
        if(agent==null || role==null)
        {
            msg("The agent or the role are null!!");
            return null;
        }

        return removeRoleFromDescription(agent,role.getClass());

    }


    public RolexAgent removeRoleFromDescription(RolexAgent agent, Class role)
         throws RoleException,IncompatibilityException,RoleLoaderException,UnusableRoleLoaderException
         {

        /* check params */
        if(agent==null || role==null)
        {
            msg("The agent or the role are null!!");
            return null;
        }

        /* now I must get all the agent's roles and check if the specified
        role is owned from the agent */

        Class roles[]=this.getOwnedRoles(agent);

        /* check if all ok */
        if(roles==null)
        {
            throw new RoleLoaderException("can't get the agent roles");
        }

        /* if the agent has no roles I can't remove a role from it, return
        the agent itself because it has no roles */
        if(roles.length==0)
        {
            msg("\n\nThe agent has no role!!!!\n\n");
            return (RolexAgent)agent;
        }

        /* now check if the agent has the role that must be removed */
        boolean found=false;

        for(int i=0;i<roles.length;i++)
        {
            if(roles[i].equals(role))
            {
                /* role founded, can remove it */
                found=true;
                msg("The agent has the role that must be removed:"+roles[i].getName());
                break;
            }
        }

        /* if no role founded the agent is ok, return it */
        if(found==false)
        {
            return (RolexAgent)agent;
        }

        /* if here I must remove the agent, that is add to the agent all the
        roles except the specified */
        Class newRoles[]=new Class[roles.length-1];

        /* ATTENTION:
        if the newRoles length is 0 the agent has only one role, remove all */
        if(newRoles.length==0)
        {
            msg("The agent has only one role, remove all roles!");
            return this.removeAllRoles(agent);
        }


        /* copy the vector */
        int j=0;
        for(int i=0;i<roles.length;i++)
        {

            /* check if the role is the removed role */
            if(!roles[i].equals(role))
            {

                /* the "i" role is not the role that must be removed,
                copy it into the new vector */
                try {
                    newRoles[j] = (Class) Class.forName(agent.getRoleClass(roles[i].getName())).newInstance();
                }
                catch (ClassNotFoundException ex) {
                }
                catch (IllegalAccessException ex) {
                }
                catch (InstantiationException ex) {
                }
                j++;
            }
        }
        agent.removeRoleInformation(role.toString());

        /* now can manipulate the agent */
        return (RolexAgent)this.addRoleFromDescription(agent,agent.getClass(),newRoles);

  }





































    /**
     * Method to remove some role sfrom an agent.
     * This method search for every role that the agent has at this time
     * and remove only the specified ones.
     * @param agent the agent to manipulate
     * @param role the description of the role
     * @return the new agent without the specified role
     * @exception IncompatibilityException if a compatibility error occurs
     * @exception RoleLoaderException if loader internal error
     * @exception RoleException if role is bad
     */
//    public RolexAgent removeMultipleRoleFromAgent(RolexAgent agent, Role role[])
//        throws RoleException,IncompatibilityException,RoleLoaderException,UnusableRoleLoaderException
//    {
//        /* check params */
//        if(agent==null || role==null || role.length==0)
//        {
//            return null;
//    }

//    /* now I must get all the agent's roles and check if the specified
//       role is owned from the agent */

//   Class roles[]=this.getOwnedRoles(agent);

        /* check if all ok */
//        if(roles==null)
//        {
//        throw new RoleLoaderException("can't get the agent roles");
//        }

        /* if the agent has no roles I can't remove a role from it, return
        the agent itself because it has no roles */
//        if(roles.length==0)
//        {
//        return (RolexAgent)agent;
//        }

        /* if here I must remove the agent, that is add to the agent all the
        roles except the specified ones */
//    Class newRoles[]=new Class[roles.length-role.length];

        /* ATTENTION:
        if the newRoles length is 0 the agent has only one role, remove all */
//      if(newRoles.length==0)
//      {
//      msg("The agent has only one role, remove all roles!");
//      return this.removeAllRoles(agent);
//      }



        /* copy the vector */
//      int k=0;
//      boolean copy=true;
//      for(int i=0;i<roles.length;i++)
//      {
         //   copy=true;

            /* check if the role is the removed role */
            //for(int j=0;j<role.length;j++)
           // {
           //     if(roles[i].equals((Role)role[j]))
           //     {
                    /* this role must be cutted off */
          //          copy=false;
           //         break;
         //       }
          //  }

            /* chek if the role must be contained */
           // if(copy==true)
          //  {
           //     newRoles[k]=roles[i];
          //      k++;
           // }
           //  }


        /* now can manipulate the agent */
        //return (RolexAgent)this.addRole(agent,newRoles);


   // }



    /**
     * Method to get all roles owned by an agent.
     * This method allow you to recognize every interface that the agent
     * implements and that is a role interface (that is a subclass of
     * interface GenericRole @see GenericRole).
     * @param agent the agent to analize
     * @return the vector of descriptors for the role
     */
    protected final Class[] getOwnedRoles(RolexAgent agent)
    {
        /*check params */
        if(agent==null)
        {
            return null;
        }

        /* obtain the roles already added: a role is an interface that
        extends GenericRole */
        Class interfaces[]=agent.getClass().getInterfaces();
        Vector roles=new Vector(interfaces.length);

        /* store every role interface*/
        for(int i=0;i<interfaces.length;i++)
        {
            msg("Analizing interface <"+interfaces[i].getName()+">");

            /* if the interface is a role interface store it */
            if(this.isRoleInterface(interfaces[i])==true)
            {
                roles.add((Class)interfaces[i]);
                msg("role interface found "+interfaces[i].getName());
            }
        }

        /* Now I have the role interfaces to add, the agent, the new role
        but I need the role implementations for previous interfaces. */
        Class desc[]=new Class[roles.size()];


        String name=null;

        for(int i=0;i<desc.length;i++)
        {
            name=((Class)roles.get(i)).getName();
            msg(name);
            desc[i]=((Class)roles.get(i));
        }


        /* all done */
        return desc;
    }

    /**
     * Get a role descriptor by the name of the class implementation or
     * the interface name.
     * @param name the name
     * @return the role descriptor
     */
/*    protected final Role getDescriptorByName(String name)
    {
        if(this.locator!=null && name!=null)
        {
            return this.locator.getDescriptorFromRoleName(name);
        }
        else
        {
            return null;
        }
    }

*/

    /**
     * Method to check if an interface is a subclass of GenericRole.
     * @param intf the interface to check
     * @param flag true if you allow a role like GenericRole, false if
     * every role must extends GenericRole
     * @return true if the interface inherit (on various levels) from
     * GenericRole, false if not (or interface param is null)
     */
    protected  boolean isRoleInterface(Class intf)
    {
        /* checks param */
        if(intf==null || intf.isInterface()==false)
        {
            return false;
        }

        /* now check if the interface is of the GenericRole */
        if(intf.equals(Role.class))
        {
            return true;
        }
        else
        {
            /* the interface is not a genericrole, get its superclass
            and try again */
            Class superInterfaces[]=intf.getInterfaces();

            /* now I must check all the interfaces */
            for(int i=0;i<superInterfaces.length;i++)
            {

                /* NOTE: superinterfaces could be GenericRole,
                use always flag=true in ricorsive way. */
                if(this.isRoleInterface(superInterfaces[i])==true)
                {
                    return true;
                }

            }

            /* if here no one interface is a GenericRole */
            return false;
        }
    }





    /**
     * Constructor for this classloader.
     * This is a null constructor that only wants the RoleLocator.
     * The role locator is used to get information abouyt the roles to be
     * loaded.
     * <BR><B>
     * You must use a different classloader for every different operations. This
     * is because class loader mantains a loaded class cache that will cause
     * LinkageErrore and LinkageException if you try to manipulate more
     * than one times the agent with the same loader.</B>
     * @param locator the role locator to be used. It should be the locator that
     * has the roles definition.
     * @exception UnusableRoleLoaderException thrown if the role loader
     * can't perform the autotset (see the autoTest method).
     */
    public RoleLoader(RoleLocator locator)
        throws UnusableRoleLoaderException
    {
        super();
        this.setRoleLocator(locator);

        /* do an autotest */
        this.autoTest();

   }


public RoleLoader() throws UnusableRoleLoaderException{
    super();
    this.autoTest();
}




    /**
     * Method to set the role loader behavior.
     * @param flag true if you want to add roles,false if you want to
     * remove roles
     */
    protected void setBehavoir(boolean flag)
    {
        this.add=flag;
    }

    /**
     * Method to set the role locator.
     * The locator should be a valid locator (but no one test is performed here)
     * and should contains the role descriptors that you want to use.
     * @param locator the role locator to use
     */
    protected void setRoleLocator(RoleLocator locator)
    {
        this.locator=locator;
    }


    /**
     * Method to check if a ctclass objects has a method.
     * Use this method to test before copy the method.
     * @param obj the ctclass object to test
     * @param function the method to search
     * @return true if the ctclass object has already this method, false if no
     */
    protected final boolean hasThisMethod(CtClass obj,CtMethod function)
    {
        /* check params */
        if(obj==null || function==null)
        {
            return false;
        }

        /* now get all the declared method */
        CtMethod inMethods[]=obj.getMethods();
        CtMethod declaredMethods[]=obj.getDeclaredMethods();

        /* now merge the array */
        CtMethod objMethods[]=new CtMethod[(inMethods.length+declaredMethods.length)];

        int k,l;

        for(k=0;k<inMethods.length;k++)
        {
            objMethods[k]=inMethods[k];
        }

        for(l=0;l<declaredMethods.length;l++)
        {
            objMethods[k]=declaredMethods[l];
            k++;
        }



        try
        {

            /* search for this method: check name,return type and params */
            for(int i=0;i<objMethods.length;i++)
            {
                if( objMethods[i].equals((CtMethod)function) ||
                    (objMethods[i].getName().equals((String)function.getName()) &&
                     objMethods[i].getReturnType().equals((CtClass)function.getReturnType()) &&
                     objMethods[i].getParameterTypes().equals((CtClass[])function.getParameterTypes()) ))
                {
                    /* found duplicated method */
                    msg("Il metodo "+function.getName()+" esiste già");
                    return true;
                }
            }

        }
        catch(NotFoundException e)
        {
            return true;
        }

        /* all done */
        return false;
    }


    /**
     * Method to check if a ctclass object has already a field.
     * @param obj the ctclass object to check
     * @param field the ctfield object to search
     * @return true if the method is already present, false otherwise
     */
    protected final boolean hasThisMember(CtClass obj,CtField field)
    {
        /* check params */
        if(obj==null || field==null)
        {
            return false;
        }

        /* now get all the fileds */
        CtField inFields[]=obj.getFields();
        CtField declaredFields[]=obj.getDeclaredFields();

        /* now merge all the fields */
        CtField objFields[]=new CtField[(inFields.length+declaredFields.length)];

        int k,l;

        for(k=0;k<inFields.length;k++)
        {
            objFields[k]=inFields[k];
        }

        for(l=0;l<declaredFields.length;l++)
        {
            objFields[k]=declaredFields[l];
            k++;
        }


        /* a member (variable) is reputed if (see JVM specification, chapter 4):
        the name is the same, the type is the same. */
        try
        {
            /* search the field */
            for(int i=0;i<objFields.length;i++)
            {
//                msg("Verifca se il membro "+objFields[i].getName()+" è ripetuto");
//                msg("dati origine:" + objFields[i].getName()+" "+objFields[i].getType().getName());
//                msg("dati destinazione:"+field.getName()+" "+field.getType().getName());


                if(objFields[i].equals((CtField)field)==true ||
                     (objFields[i].getName().equals((String)field.getName()) &&
                        objFields[i].getType().getName().equals(field.getType().getName())))

                {
                    msg("Il campo "+field.getName()+" esiste già");
                    return true;
                }
            }

        }
        catch(Exception e)
        {
            //System.out.println("Eccezione nella copia dei membri ");
            e.printStackTrace();
            return true;
        }

        /* all done */
        return false;
    }



    /**
     * Method to check if a method is a object method, that is a method inerhit
     * from java.lang.Object.
     * @param function the ctmethod
     * @return true if the method is one of the object class
     */
    protected boolean isObjectMethod(CtMethod function)
    {
        /* check params */
        if(function==null)
        {
            return false;
        }

        try
        {
            /* now load all the object methods */
            Method m[]=Class.forName("java.lang.Object").getMethods();

            if(m!=null)
            {
                for(int i=0;i<m.length;i++)
                {
                    if( m[i].getName().equals((String)function.getName()) &&
                        m[i].getModifiers()==function.getModifiers() )
                    {
                        /* names and modifiers are the same, check parameters.
                        I need check the parameter name because I have to check a
                        ctclass object and a class object */
                        CtClass param[]=function.getParameterTypes();
                        Class param2[]=m[i].getParameterTypes();

                        if(param2==null || param==null || param.length!=param2.length)
                        {
                            continue;
                        }
                        else
                        {
                            boolean equal=true;

                            for(int j=0;j<param.length;j++)
                            {
                                if(!param[j].getName().equals((String)param2[j].getName()))
                                {
                                    equal=false;
                                    break;
                                }
                            }

                            if(equal==true &&
                                m[i].getReturnType().getName().equals((String)function.getReturnType().getName()))
                            {
                                return true;
                            }
                        }
                    }

                }
            }

            /* if here the methods are not equals */
            return false;

        }
        catch(NotFoundException e)
        {
            return false;
        }
        catch(ClassNotFoundException e)
        {
            return false;
        }
    }



    /**
     * Method to check if a ctclass object has an interface.
     * @param obj the object to check
     * @param interf the interface to search
     * @return true if the obj implements the interface, false if not.
     */
    protected final boolean hasThisInterface(CtClass obj,CtClass interf)
    {
        /* check params */
        if(obj==null || interf==null)
        {
            return false;
        }

        try
        {

            /* get all the interfaces */
            CtClass allInterfaces[]=obj.getInterfaces();

            /* search the specified interface */
            for(int i=0;i<allInterfaces.length;i++)
            {
                if( allInterfaces[i].equals((CtClass)interf) ||
                    allInterfaces[i].getName().equals((String)interf.getName()) )
                {
                    /* found the interface */
                    return true;
                }
            }

            /* if here the obj has no the interface */
            return false;

        }
        catch(NotFoundException e)
        {
            /* problem during interface search */
            return false;
        }
    }


    /**
     * Method to test compatibility between roles.
     * This method ask to the locator if the roles are compatibles.
     * @param role1 the role you want to check
     * @param roles a list of role to check with the role1. IT is tested
     * if the role1 is compatibility with all the roles.
     * @return true if compatibility is ok, false if not.
     * @exception RoleLoaderException thrown if the locator is unreacheble
     * @exception IncompatibilityException thrown if the roles are not
     * compatibles
     */
    protected boolean checkCompatibility(Role role1,
                                            Role roles[])
            throws RoleLoaderException,IncompatibilityException
    {
        /* check params */
        if(role1==null || roles==null || roles.length==0)
        {
            /* no roles to check, always compatibles */
            return true;
        }

        /* check if the locator is on */
/*        if(this.locator==null)
        {
            throw new RoleLoaderException("Can't contact the locator");
        }
*/
        /* test all the roles */
/*        for(int i=0;i<roles.length;i++)
        {
            if( this.locator.areCompatibles(role1,roles[i])==false)
            {
                /* found a role incompatible, stop here */
 //               return false;
//            }
//        }

        /* if here all ok */
        return true;
    }

    /**
     * Method to check compatibility into a vector of role descriptors.
     * @param roles the role descriptor.
     * @exception IncompatibilityException if the roles are incompatibles
     * @exception RoleLoader if the locator can't be contacted
     */
    protected void checkCompatibility(Role roles[])
        throws IncompatibilityException
    {
        /* check params */
        if(roles==null || roles.length==0)
        {
            return;
        }

        /* check all the roles */
        /* I need to do a double check */
//        for(int i=0;i<roles.length;i++)
//        {
//            for(int j=i;j<roles.length;j++)
//            {
//                this.locator.areCompatibles(roles[i],roles[j]);
//            }
//        }

    }





    /**
     * Method to check the classloader cache.
     * If you try to load a class (agent) that has been already loaded and do
     * bytecode manipulation on it you will receive a LinkageError. This is
     * because the defineClass(..) method try to load two different bytecodes
     * with the same name -> namespace collision. This method allow you to know
     * if the classloader is usable to load the agent.
     * Use this method only for check on the class you want to manipulate.
     * @param agentName the agent you want to load and manipulate
     * @exception UnusableRoleLoader thrown if the classloader can't manipulae
     * agent bytecode. Create a new RoleLoader and use it.
     *
     */
    protected void checkRoleLoaderCache()
        throws UnusableRoleLoaderException
    {

        /* I must  extract everything from the role stack */
        Stack myStack=this.cloneRoleStack();

        if(myStack==null)
        {
            return;
        }

        /* now get all the names */
        String names[]=null;

        while(this.checkStack())
        {
            names=this.popFromStack();

//            for(int i=0;i<names.length;i++)
//            {
//                if(names[i]!=null)
//                {
                    /* ask to the classloader cache if the class is already
                    loaded */
 //                   if(this.findLoadedClass(names[i])!=null)
 //                   {
 //                       throw new UnusableRoleLoaderException("Class "+names[i]+" already loaded");
 //                   }
 //               }
 //           }
        }

        /* recovery the stack */
        this.setRoleStack(myStack);



    }


    /**
     * Method to test the loader.
     * This method try to load the loader himself. If the methdo fails an
     * Unusable exception is thrown.
     * @exception UnusableRoleLoaderException thrown if the loader couldn't
     * load himself.
     */
    public void autoTest()
        throws UnusableRoleLoaderException
    {
        /* try to load the loader himself */
 /*       try
        {
            crono.addTimeEvent(100,System.currentTimeMillis(),"Role Loader starts autotest");
            //MagicPrintStream.printMsg("Autotesting the role loader...");
            this.loadClass(this.getClass().getName(),true);
            //MagicPrintStream.printMsg("success!");
            crono.addTimeEvent(100,System.currentTimeMillis(),"Role Loader ends autotest");
        }
        catch(Exception e)
        {
            MagicPrintStream.printMsg("The role loader can't perform autotest:");
            e.printStackTrace();
            /* can't load, the loader is unusable */
 //           throw new UnusableRoleLoaderException("The loader can't pass the autotest");
 //       }

    }


    /**
     * Method to put a string array on the stack.
     * This method construct a string array to put on the stack. The first
     * string is the class name to be loaded at this level, the others are
     * the roles names.
     * @param className the name of the class at this level
     * @param roleNames the name of the roles to add at the class
     */
    protected void addToStack(String className,String[] roleNames)
    {
        int index=0;
        boolean changedChain=false;

        msg("INIZIO FUNZIONE ADDTOSTACK con valori "+className);

        if(className==null && roleNames==null)
        {
            return;
        }

        /* check params */
        if(className==null )
        {
            /* the agent name is null, I must extract it from the roleNames
            arry */
            for(int i=0;i<roleNames.length;i++)
            {
                if(roleNames[i]!=null)
                {
                    className=roleNames[i];
                    index=i;
                    changedChain=true;
                    break;
                }
            }
        }


        /* if the role names are null I must put in the stack a null vector */
        if(roleNames==null)
        {
            String v[]=new String[2];
            v[1]=null;
            v[0]=className;
            this.roleStack.push((String[])v);
            return;

        }

        /* now construct a single vector */
        String tmp[]=new String[(1+roleNames.length)];

        /* reset the array */
        for(int i=0;i<tmp.length;i++)
        {
            tmp[i]=null;
        }

        /* copy the names into the array */
        tmp[0]=className;

        msg("Calculating the entry to add to the stack!");
        if(index==0 && changedChain==false)
        {
            for(int i=1;i<tmp.length ;i++)
            {
                tmp[i]=roleNames[i-1+index];
            }
        }
        else
        if(index>0)
        {
            for(int i=1;i<tmp.length && i<roleNames.length-index ;i++)
            {
                tmp[i]=roleNames[i-1+index];
            }

        }

        /* put the array into the stack */
        if(this.roleStack==null)
        {
            this.roleStack=new Stack();
        }

        msg("Adding an entry to the role stack!");
        this.roleStack.push((String[])tmp);
    }

    /**
     * Method to get an array from the stack.
     * This method doesn't remove the vector from the stack.
     * @return the first string array into the stack
     */
    protected String[] peekFromStack()
    {
        if(this.roleStack!=null && this.roleStack.isEmpty()==false)
        {
            return (String[])this.roleStack.peek();
        }
        else
        {
            return null;
        }
    }


    /**
     * Method to pop a string array from the stack.
     * @return the removed array
     */
    protected String[] popFromStack()
    {
        if(this.roleStack!=null && this.roleStack.isEmpty()==false)
        {
            return (String[])this.roleStack.pop();
        }
        else
        {
            return null;
        }
    }


    /**
     * Method to clone the stack.
     * @return the cloned stack
     */
    protected Stack cloneRoleStack()
    {
        /* check if the stack is on */
        if(this.checkStack())
        {
            return (Stack)this.roleStack.clone();
        }
        else
        {
            return null;
        }
    }

    /**
     * Method to insert a stack.
     * @param stack the new role stack
     */
    protected void setRoleStack(Stack stack)
    {
        this.roleStack=stack;
    }

    /**
     * Method to check the stack.
     * The method checks if the stack is null or empty.
     * @return true if the stack is not empty, false if empty or null
     */
    protected boolean checkStack()
    {
        if(this.roleStack==null || this.roleStack.isEmpty())
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    /**
     * Method to reverse the stack.
     * This method change the stack order, so the first argument
     * became the last.
     */
    protected void reverseRoleStack()
    {
        /* check if the stack is on */
        if(!this.checkStack())
        {
            return;
        }


        /* create a new stack */
        Stack reversed=new Stack();

        /* to reverse the stack every thing is popped from the original
        stack is immediatly pushed into the reversed stack */
        while(this.checkStack())
        {
            reversed.push((String[])this.popFromStack());
        }

        /* now set the new stack */
        this.setRoleStack(reversed);
    }

    /**
     * Method to get the stack size.
     * @return the size of the stack
     */
    protected int getRoleStackSize()
    {
        if(this.roleStack!=null)
        {
            return this.roleStack.size();
        }
        else
        {
            return 0;
        }
    }

    /**
     * Method to add a class name to the already copied objects.
     * @param the class name
     */
    protected void alreadyCopied(String className)
    {
        /* check params */
        if(className==null)
        {
            return;
        }


        /* check the vector */
        if(this.alreadyCopied==null)
        {
            this.alreadyCopied=new Vector();
        }

        /* add the class name */
        this.alreadyCopied.add((String)className);
    }

    /**
     * Method to know if a class is already copied.
     * @param className the name of class
     * @return true if so, false if not.
     */
    protected boolean isAlreadyCopied(String className)
    {
        /* check params */
        if(className==null)
        {
            return false;
        }

        /* check the vector */
        if(this.alreadyCopied==null || this.alreadyCopied.isEmpty())
        {
            return false;
        }
        else
        {
            return this.alreadyCopied.contains((String)className);
        }

    }





    /**
     * Method to add interfaces to a ctclass object.
     * This method is used to set all the role interfaces of a ctclass
     * object.
     * @param src the ctclass object into which copy the interfaces
     * @param interfaces the interfaces to set
     * @return the ctclass object modified
     */
    protected final CtClass addInterfaces(CtClass src,CtClass interfaces[])
    {
        /* check params */
        if(src==null || interfaces==null || interfaces.length==0)
        {
            return src;
        }

        /* store the interfaces to add in a temporary vector */
        Vector tmp=new Vector(interfaces.length);

        /* now add the interfaces */
        for(int i=0;i<interfaces.length;i++)
        {
            /* test if the interface is already setted */
            if(!this.hasThisInterface(src,interfaces[i]))
            {
                /* add the interfaces */
                tmp.add((CtClass)interfaces[i]);
            }
        }




        /* copy all the interfaces */
        for(int i=0;i<tmp.size();i++)
        {
            src.addInterface((CtClass)tmp.get(i));
        }


        /* all done */
        return src;
    }


    /**
     * Method to copy all the members (methods and argument) declaration.
     * @param dest the CtClass object into which copy all
     * @param src the object from which copy members.
     * @return the ctclass object modified
     */
    protected final CtClass copyMembers(CtClass src,CtClass dest,CtClass superclass)
        throws CannotCompileException,NotFoundException
    {
        /* check params */
        if(src==null || dest==null)
        {
            return dest;
        }


        /* check if the class is already been copied at another level */
        if(this.isAlreadyCopied(src.getName()))
        {
            return dest;
        }

        crono.addTimeEvent(100,System.currentTimeMillis(),"Role Loader starts copying a level");


        /* now add all the method.
        I must check if the class has already this method and then
        copy it into the class */
        CtMethod toAdd[]=src.getDeclaredMethods();
        CtMethod copy=null;

        /* check if there is something to add */
        if(toAdd==null || toAdd.length==0)
        {
            return dest;
        }

        /* add al the methods */
        for(int i=0;i<toAdd.length;i++)
        {

            /* check if the mehod is already present */
            if(!this.hasThisMethod(dest,toAdd[i])
                && (!Modifier.isFinal(toAdd[i].getModifiers())) &&
                 (!Modifier.isStatic(toAdd[i].getModifiers())) )
            {


                /* add it */
                copy=CtNewMethod.copy(toAdd[i],dest,null);
                /* ATTENTION: if the method is protected or private the
                reflection api cause a IllegalAccessException */
                //copy.setModifiers(Modifier.PUBLIC);
                dest.addMethod(copy);
                msg("copiato il metodo "+copy.getName());
            }
            else
            if((!Modifier.isFinal(toAdd[i].getModifiers())) &&
                 (!Modifier.isStatic(toAdd[i].getModifiers())) &&
                 (!this.isObjectMethod(toAdd[i])))
            {
                /* generate a warning */
                this.addWarning(LoaderWarnings.duplicatedMethod,dest,toAdd[i],toAdd[i].getName(),src);

            }
        }

        /* now add all the fields */
        CtField fields[]=src.getDeclaredFields();
        CtField addingField=null;

        /* check if there is something to do */
        if(fields==null || fields.length==0)
        {
            return dest;
        }

        /* add all the fields. To add I must copy the field and its
        access specificator. */
        for(int i=0;i<fields.length;i++)
        {


            if(!this.hasThisMember(dest,fields[i]) )
            {
                msg("Preparazione alla copia di "+fields[i].getName());
                /* construct the adding field */
                addingField=new CtField(fields[i].getType(),fields[i].getName(),dest);

                /* IMPORTANT:
                the ctfield has a default modifier null, so set it to the
                correct type (private,public,ecc). */
                addingField.setModifiers(fields[i].getModifiers());


                dest.addField(addingField);
                msg("copiato il campo "+addingField.getName());
            }
            else
            {
                /* generate a warning */
                this.addWarning(LoaderWarnings.duplicatedVariableName,dest,fields[i],fields[i].getName(),src);

                /* copy the method with a different name */
                /* construct the adding field */
                //addingField=new CtField(fields[i].getType(),"pluto",dest);

                /* IMPORTANT:
                the ctfield has a default modifier null, so set it to the
                correct type (private,public,ecc). */
                //addingField.setModifiers(fields[i].getModifiers());


                //dest.addField(addingField);
                //msg("copiato il campo "+addingField.getName());

            }

        }

        /* add the copied class to the copied database */

        this.alreadyCopied(src.getName());


                crono.addTimeEvent(100,System.currentTimeMillis(),"Role Loader ends copying a level");
        /*all done */
        return dest;
    }


    /**
     * Method to copy fields value.
     * This method accept two instance and two class type.
     * @param src the instance src
     * @param srcClass the source class type
     * @param dest the destination implementation
     * @param destClass the destination class type
     * @return the number of fields copied
     */
    protected final int copyMembersValue(Object src,Class srcClass,
                                            Object dest, Class destClass)
        throws IllegalArgumentException,IllegalAccessException
    {
        /* check params */
        if(src==null || srcClass==null || dest==null || destClass==null)
        {
            /* can't proceed */
            return 0;
        }

        int added=0;


        /* get the fields */
        Field[] srcFields,destFields;
        srcFields=srcClass.getDeclaredFields();
        destFields=destClass.getDeclaredFields();

        if(destFields==null || srcFields==null)
        {
            return 0;
        }

        boolean originalSrcAccessType=true;    /* used to store the original
                                                    access type of the fields */
        boolean originalDestAccessType=true;

        for(int i=0;i<srcFields.length;i++)
        {
            for(int j=0;j<destFields.length;j++)
            {
                if(destFields[j].getName().equals(srcFields[i].getName()) &&
                    destFields[j].getType().equals(srcFields[i].getType()))
                {
                    /* store the access type */
                    originalSrcAccessType=srcFields[i].isAccessible();
                    originalDestAccessType=destFields[j].isAccessible();

                    /* make the dest field accesible */
                    destFields[j].setAccessible(true);

                    /* make accesible the src member */
                    srcFields[i].setAccessible(true);

                    if(debug)
                    {
                        msg("Found the member "+destFields[j].getName()+" ("+this.modifierTypeToString(destFields[i])+")");
                    }


                    /* copy the field */
                    /* to cpy the fields simply transfer it from src to dest,
                    note that the get method automatically wrapped the primitive
                    members, so the value passed to the set method is right. */

                    /* Attention:
                    copying final members causes an IllegalAccessException!!!!! */
                    if(!Modifier.isFinal(destFields[j].getModifiers()) &&
                        !Modifier.isTransient(destFields[j].getModifiers()) &&
                        !Modifier.isVolatile(destFields[j].getModifiers()) &&
                        !Modifier.isStatic(destFields[j].getModifiers()))
                    {
                        destFields[j].set(dest,srcFields[i].get(src));
                        added++;

                        if(debug)
                        {   msg("copy done");    }

                    }


                    /* now set the member at the accesible type orignal */
                    destFields[j].setAccessible(originalDestAccessType);
                    srcFields[i].setAccessible(originalSrcAccessType);
                }
            }

        }

        /** all done */
        return added;
    }



    /**
     * Method to get roles name from a role Class array.
     * @param the roles class array
     * @return the string array
     */
    protected final String[] getRoleNamesFromClass(Class[] roles)
    {
        /* check params */
        if(roles==null || roles.length==0)
        {
            /* the names are null, return a vector with null string */
            String v[]=new String[1];
            v[0]=null;
            return v;
        }

        /* construct the name array */
        String ret[]=new String[roles.length];

        for(int i=0;i<roles.length;i++)
        {
            if(roles[i]!=null)
            {
                ret[i]=roles[i].getName();
            }
            else
            {
                ret[i]=null;
            }
        }

        /* all done */
        return ret;

    }


    /**
     * Method to compute the stack chain.
     * This method start from the agent and the roles to add and
     * search along the inheritance chain where every role must be loaded.
     * @param agent the agent class
     * @param roles the role to be added
     */
    protected final void computeInheritanceChain(Class agent,Class[] roles)
    {
        /* check params */
        if(agent==null )
        {
            /* you have no specified the agent, empty the stack */
            while(this.popFromStack()!=null)
            {
            }

            return;
        }

        crono.addTimeEvent(100,System.currentTimeMillis(),"Role Loader starts computing stack");

        /* now proceed*/


        /* at the agent level I must push the agent and its roles */
        String s[]=this.getRoleNamesFromClass(roles);
        for(int i=0;s!=null && i<s.length;i++)
                msg("The first entry of the stack is "+s[i]);

        this.addToStack(agent.getName(),this.getRoleNamesFromClass(roles));


        String agentName=null;
        boolean stop=true;

        /* now I must go up a level and watch what I find */

        do
        {

            stop=true;

            /* get the agent name for this level */
            if(agent!=null && (agent=(agent.getSuperclass()))!=null)
            {
                agentName=agent.getName();


                /*ATTENTION: if the agentName is Object I must set it
                to null, so a role could take its place */
                if(agentName.equals((String)"java.lang.Object"))
                {
                    agentName=null;
                }
            }
            else
            {
                agentName=null;
            }

            if(roles!=null)
            {
                for(int i=0;i<roles.length;i++)
                {
                    if(roles[i]!=null)
                    {
                        roles[i]=roles[i].getSuperclass();

                        /* if adding a null roles stop here */
                        if(roles[i]!=null)
                        {
                            stop=false;
                        }

                        if(roles[i].getName().equals((String)"java.lang.Object"))
                        {
                            roles[i]=null;
                        }
                    }
                }
            }
            else
            {
                stop=true;
            }

            /* ATTENTION: if the agent is null and all the roles are null
            I must stop here */
            boolean end=true;

            if(agentName==null)
            {
                if(roles!=null)
                {
                    for(int i=0;i<roles.length;i++)
                    {
                        if(roles[i]!=null)
                        {
                            end=false;
                        }
                    }
                }
            }
            else
            {
                end=false;
            }

            if(end==false)
            {
                /* add to the stack this level */
                this.addToStack(agentName,this.getRoleNamesFromClass(roles));
            }

        }
        while(stop==false || agentName!=null);


        crono.addTimeEvent(100,System.currentTimeMillis(),"Role Loader ends computing stack");
    }



    /**
     * Method to generate a inheritance level. The inheritance chain must
     * be computed.
     * This method creates the pools needed and the ctclass objects, then
     * add interfaces and members and return the bytecode manipulated.
     */
    protected final Class generateLevel()
        throws NotFoundException,CannotCompileException,IOException
    {
        /* check if the stack is ready */
        if(!this.checkStack())
        {
            /* can't proceed */
            return null;
        }


        /* get the current stack level */
        String names[]=this.popFromStack();

        /* extract agent name and roles names */
        String agentName=names[0];
        String roleNames[]=new String[names.length-1];
        //System.arraycopy(names,1,roleNames,0,roleNames.length);

        for(int i=0;i<roleNames.length;i++)
        {
            roleNames[i]=names[i+1];
        }

        /* now there are these cases:
        1) agentName is null or java.lang.object and the role names
        are not null: the superclass of this level must be one of the roles;
        2) agentName is null and so the roles, nothing to do;
        3) agentName is not null and so the roles, copy everything
        4) agentName is not null but the roles so,return the agent.
        */



        /* create the pools */
        ClassPool agentPool=ClassPool.getDefault();
        ClassPool rolePool=ClassPool.getDefault();

        /* add the classpath */
        this.addClasspathToPoll(agentPool);
        this.addClasspathToPoll(rolePool);

        /* the ctclass objects */
        CtClass agentClass=null;
        CtClass rolesClass[]=null;



        /* this flag is used to say if the first role must be skipped becaues
        it's used like an agent. Infact if the agent name is null I must use
        a role like agent base class */
        boolean skipFirst=false;

        /* now check if the agent is null or not */
        if(agentName!=null)
        {
            /* get its pool */
            agentClass=agentPool.get(agentName);
        }
        else
        if(agentName==null || agentName.equals((String)"java.lang.Object"))
        {
            /* there is no agent, so I must substitute the agent with a role*/
            for(int i=0;i<roleNames.length;i++)
            {
                if(roleNames[i]!=null)
                {
                    agentClass=agentPool.get(roleNames[i]);
                    skipFirst=true;
                    break;
                }
            }
        }


        /* now I have the agentClass, make the role classes */
        Vector tmp=new Vector();

        for(int i=0;i<roleNames.length;i++)
        {
            /* check if the role must be skipped and if it's no null */
            if(roleNames[i]!=null && skipFirst==false)
            {
                tmp.add((CtClass)rolePool.get(roleNames[i]));
            }
            else
            if(roleNames[i]!=null && skipFirst==true)
            {
                /* this is the first role to skip, don't skip
                the others */
                skipFirst=false;
            }
        }

        /* now I know how many role ctclass have, create a ctclass array */
        if(!(tmp.size()==0))
        {
            rolesClass=new CtClass[tmp.size()];
            /* copy all the ctclasses */
            for(int i=0;i<rolesClass.length;i++)
            {
                rolesClass[i]=(CtClass)tmp.get(i);
            }

        }
        else
        {
            /* if here it means that on this agent I must do nothing, no
            manipulation. */

            /*sets the superclass for this class */
            if(this.superclass!=null)
            {
                msg("Imposto la superclasse a "+this.superclass.getName()+" per la classe "+agentClass.getName());
                agentClass.setSuperclass(this.superclass);
            }

            /* set the current superclass */
  //          this.superclass=agentClass;

            byte code[]=((CtClass)agentPool.get(agentName)).toBytecode();
            return this.defineClass(agentName,code,0,code.length);
        }



        /* release the vector */
        tmp=null;


        /* now I have all the classes, init bytecode manipulation */
        for(int i=0;i<rolesClass.length;i++)
        {
            /* copy the interfaces */
            agentClass=this.addInterfaces(agentClass,rolesClass[i].getInterfaces());

            /* copy all the members */
            agentClass=this.copyMembers(rolesClass[i],agentClass,this.superclass);

        }

        /* sets the superclass */
        if(this.superclass!=null)
        {
            msg("Imposto la superclasse a "+this.superclass.getName()+" per la classe "+agentClass.getName());
            agentClass.setSuperclass(this.superclass);
        }



        /* set the current superclass */
        this.superclass=agentClass;

        /* bytecode manipulation completed for this level */
        byte[] code= ((CtClass)agentPool.get(agentName)).toBytecode();


        /* define the class */
       return this.defineClass(agentName,code,0,code.length);


    }


    /**
     * Method to add a warning. This method set a new warning in the warning
     * table.
     * @param type the warning type
     * @param src the class in which the warning occur
     * @param cause the class that generate the warning
     * @param by the warning generator
     */
    protected void addWarning(int type,CtClass src,Object cause,String causeName,CtClass by)
    {
        /* don't check any parameter here, allow the user to set a unknow
        warning type with null src and cause. All it's possible! */

        /* check if the warning is on */
        if(this.warnings==null)
        {
            this.warnings=new Vector();
        }

        /* now create the warning */
        Warning w=new Warning(type,src,by,cause,causeName);

        /* store the warning */
        this.warnings.add((Warning)w);
    }


    /**
     * Method to get the warnings count.
     * @return the number of warning of the last manipulation.
     */
    public int getWarningCount()
    {
        if(this.warnings==null || this.warnings.isEmpty())
        {
            return 0;
        }
        else
        {
            return this.warnings.size();
        }
    }


    /**
     * Method to get all warnings.
     * @return a warning array
     */
    public Warning[] getWarnings()
    {
        /* check the warning table */
        if(this.warnings==null || this.warnings.size()==0)
        {
            return null;
        }

        /* construct a warning vector */
        Warning warns[]=new Warning[this.warnings.size()];

        /* copy every warning */
        for(int i=0;i<warns.length;i++)
        {
            warns[i]=(Warning)this.warnings.get(i);
        }

        /* all done */
        return warns;

    }







    //-----------------------------------------------------
    /* debug operations */


    public void stampa_stack()
    {
        String[] tmp=null;
        int level=0;
        Stack s=(Stack)this.roleStack.clone();
        //System.out.println("\nDentro alla funzione stampa_stack()\n");

        msg("+++++++++++++++++++++++++++++++++++++++++++++");

        while(this.roleStack.isEmpty()==false)
        {
            level++;
            tmp=(String[])this.roleStack.pop();

                msg("Livello dello stack "+level);

            for(int i=0;i<tmp.length;i++)
            {

                msg(tmp[i]);
            }
        }

        msg("+++++++++++++++++++++++++++++++++++++++++++++");

        this.roleStack=s;
    }


    /**
     * Print member type.
     */
    protected final String modifierTypeToString(Field f)
    {
        if(f==null)
        {   return null;    }

        java.lang.reflect.Modifier m=new java.lang.reflect.Modifier();

        if(m.isAbstract(f.getModifiers()))
        {
            return "abstaract";
        }
        else
        if(m.isFinal(f.getModifiers()))
        {
            return "final";
        }
        else
        if(m.isInterface(f.getModifiers()))
        {
            return "interface";
        }
        else
        if(m.isNative(f.getModifiers()))
        {
            return "native";
        }
        else
        if(m.isPrivate(f.getModifiers()))
        {
            return "private";
        }
        else
        if(m.isProtected(f.getModifiers()))
        {
            return "protected ";
        }
        else
        if(m.isPublic(f.getModifiers()))
        {
            return "public";
        }
        else
        if(m.isStatic(f.getModifiers()))
        {
            return "static";
        }
        else
        if(m.isTransient(f.getModifiers()))
        {
            return "transient";
        }
        else
        if(m.isVolatile(f.getModifiers()))
        {
            return "volatile";
        }

        return null;
    }

     protected final String modifierTypeToString(Method f)
    {
        if(f==null)
        {   return null;    }

        java.lang.reflect.Modifier m=new java.lang.reflect.Modifier();

        if(m.isAbstract(f.getModifiers()))
        {
            return "abstaract";
        }
        else
        if(m.isFinal(f.getModifiers()))
        {
            return "final";
        }
        else
        if(m.isInterface(f.getModifiers()))
        {
            return "interface";
        }
        else
        if(m.isNative(f.getModifiers()))
        {
            return "native";
        }
        else
        if(m.isPrivate(f.getModifiers()))
        {
            return "private";
        }
        else
        if(m.isProtected(f.getModifiers()))
        {
            return "protected ";
        }
        else
        if(m.isPublic(f.getModifiers()))
        {
            return "public";
        }
        else
        if(m.isStatic(f.getModifiers()))
        {
            return "static";
        }
        else
        if(m.isTransient(f.getModifiers()))
        {
            return "transient";
        }
        else
        if(m.isVolatile(f.getModifiers()))
        {
            return "volatile";
        }

        return null;
    }


    /**
     * Debug to console
     */
    public void msg(String msg)
    {
        MagicPrintStream.printRoleLoaderMessage("[RoleLoader "+this.myNumber+"] - "+msg );
    }

    public RolexAgent addRole(RolexAgent agent, Role role) throws
            RolexException {
        Role roles[] = new Role[1];
        roles[0] = role;
        try{
            return (RolexAgent) this.addRole(agent, roles);
        }catch(Exception e){
            System.out.println("Eccezione "+e);
            e.printStackTrace();
            throw new RolexException("Eccezione");
        }
    }


    public RolexAgent addRole(RolexAgent agent, Class role) throws
           RolexException {
       Class roles[] = new Class[1];
       roles[0] = role;
       try{
           return (RolexAgent) this.addRoleFromDescription(agent, agent.getClass(), roles);
       }catch(Exception e){
           System.out.println("Eccezione "+e);
           e.printStackTrace();
           throw new RolexException("Eccezione");
       }
   }

    public RolexAgent removeRole(RolexAgent agent, Role role) throws
            RolexException {
        try {
            return (RolexAgent) this.removeSingleRoleFromAgent(agent,role);
        }catch(Exception e) {
            System.out.println("Eccezione"+e);
            e.printStackTrace();
            throw new RolexException("Eccezione");
        }
    }


    public RolexAgent removeRole(RolexAgent agent, Class role) throws
            RolexException {
        try {
            return (RolexAgent) this.removeRoleFromDescription(agent,role);
        }catch(Exception e) {
            System.out.println("Eccezione"+e);
            e.printStackTrace();
            throw new RolexException("Eccezione");
        }
    }


}







