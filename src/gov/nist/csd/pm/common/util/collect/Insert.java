package gov.nist.csd.pm.common.util.collect;

import java.util.List;

/**
 * Created by IntelliJ IDEA. User: R McHugh Date: 6/30/11 Time: 5:19 PM To change this template use File | Settings | File Templates.
 */
public enum Insert implements InsertionMethod{



    /**
	 * @uml.property  name="bEFORE"
	 * @uml.associationEnd  
	 */
    BEFORE(new InsertionMethod(){

        @Override
        public <T> void insert(T obj, T ref, List<T> coll) {
                if(coll != null && coll.contains(ref)){
                    coll.add(coll.indexOf(ref), obj);
                }

        }
    }),
    /**
	 * @uml.property  name="aFTER"
	 * @uml.associationEnd  
	 */
    AFTER(new InsertionMethod(){

        @Override
        public <T> void insert(T obj, T ref, List<T> coll) {
            if(coll != null && coll.contains(ref)){
                int idx = coll.indexOf(ref) + 1;
                if(idx < coll.size()){
                    coll.add(idx, obj);
                }
                else{
                    coll.add(obj);
                }

            }
        }
    }),
    /**
	 * @uml.property  name="bEGINNING"
	 * @uml.associationEnd  
	 */
    BEGINNING(new InsertionMethod(){

        @Override
        public <T> void insert(T obj, T ref, List<T> coll) {
            if(coll != null && coll.size() > 0){
                coll.add(0, obj);
            }
            else{
                coll.add(obj);
            }
        }
    }),
    /**
	 * @uml.property  name="eND"
	 * @uml.associationEnd  
	 */
    END(new InsertionMethod() {
        @Override
        public <T> void insert(T obj, T ref, List<T> coll) {
            if(coll != null){
                coll.add(obj);
            }
        }
    });





    /**
	 * @uml.property  name="_method"
	 * @uml.associationEnd  
	 */
    private InsertionMethod _method;

    Insert(InsertionMethod method) {
        _method = method;
    }


    public <T> void insert(T obj, T ref,  List<T> coll) {
        _method.insert(obj, ref, coll);
    }




}
interface InsertionMethod{
       public <T> void insert(T obj, T ref, List<T> coll);
   }
