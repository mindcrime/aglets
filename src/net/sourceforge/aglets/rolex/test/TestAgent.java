package net.sourceforge.aglets.rolex.test;

import net.sourceforge.aglets.rolex.*;
import net.sourceforge.aglets.rolex.descriptors.OperationDescriptor;
import java.util.Hashtable;
import java.lang.reflect.*;
import classes.net.sourceforge.aglets.rolex.test.*;
import classes.net.sourceforge.aglets.rolex.test.*;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class TestAgent implements RolexAgent{


    public Hashtable roleTable = new Hashtable();

    public Object act(OperationDescriptor operation) throws RolexException{
        throw new RolexException("");
    }


        public String getRoleClass(String intf){
            return (String) roleTable.get(intf);
        }

        /**
         * Add interface-class couple to records of agent
         * @param intf String interface's name of role
         * @param roleclass String class's name of role
         * @return boolean return if the operation has been termined succesfully
         */
        public boolean storeRoleInformation(String intf, String roleclass){
            roleTable.put(intf,roleclass);
            return true;
        }

        /**
         * Remove interface-class couple to records of agent
         * It's not necessary to pass the class name because the interface is the key of hashtable
         * @param intf String interface's name of role
         * @param roleclass String class's name of role
         * @return boolean return if the operation has been termined succesfully
         */

        public boolean removeRoleInformation(String intf){
            roleTable.remove(intf);
            return true;
        }



        public void dumpRoleTable(){
            System.out.println("\n\tTabella ruoli\n");

            java.util.Enumeration enumeration = roleTable.elements();

            while(enumeration.hasMoreElements()){
                String key = (String) enumeration.nextElement();
                System.out.println(key+": "+roleTable.get(key));
            }
        }


        public static void dumpAgent(Class myself){
            System.out.println("\n\tInformazioni agente\n");
            System.out.println("Classe: "+myself);

            //Class myself = this.getClass();
            Class intf[] = myself.getInterfaces();

            for(int i=0; i<intf.length;i++)
                System.out.println("\tInterfaccia: "+intf[i].getName());

            Method m[] = myself.getDeclaredMethods();

            for(int i=0; i<m.length;i++)
                System.out.println("\tMetodo: "+m[i].getName());


        }


/*        public TestAgent(){
            System.out.println("ciao costruttore");
        }
*/


public static void main(String argv[]) {
    try{
        TestAgent ta = new TestAgent();

        ta.dumpAgent(ta.getClass());

        RoleLoader loader = new RoleLoader();

        /* Prova passando al metodo addRole l'istanza del ruolo */
  //    Object o = loader.addRole(ta, new TestRole());

        /* Prova passando al metodo addRole la classe del ruolo */
        Object o_with    = loader.addRole(ta,TestRole.class);
 //       Object ob_with    = loader.addRole(ta,TestRole_2.class);

        System.out.println("Class loader " + o_with.getClass().getClassLoader());

   //   RolexAgent ra =  (RolexAgent) o.getClass().newInstance();



        ta.dumpAgent(o_with.getClass());
        if (o_with instanceof RolexAgent) {
            System.out.println("RolexAgent");
  //         ((TestAgent)o).dumpAgent(o.getClass());
  //         ((TestAgent)o).dumpRoleTable();
        }

        /* Prova passando al metodo removeRole l'istanza del ruolo */
       Object o_without = loader.removeRole(ta,new TestRole());
//       Object ob_without = loader.removeRole(ta,new TestRole_2());

       /* Prova passando al metodo removeRole l'istanza del ruolo */
//       Object ob = loader.removeRole(ta,TestRole.Class);

       System.out.println("Class loader " + o_without.getClass().getClassLoader());

       ta.dumpAgent(o_without.getClass());


    }catch(Exception e){
        //System.out.println("Causa:" + e.getCause().toString());
        e.printStackTrace();
    }
}


}




