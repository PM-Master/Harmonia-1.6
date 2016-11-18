package gov.nist.csd.pm.common.graphics;

import java.awt.*;


//Alignment interface, enumerations and default alignment implementations
public interface Alignment{
	public void align(Component one, Component two);
	
	

	/**
	 * Static enum for horizontal alignment
	 * @author  Administrator
	 */
	
	public static abstract class DefaultAlignments{
		//Default alignment implementation - do nothing by default, aka pass-through
		/**
		 * @uml.property  name="pASS_THROUGH_ALIGN"
		 * @uml.associationEnd  
		 */
		private static final Alignment PASS_THROUGH_ALIGN = new Alignment(){
			@Override
			public void align(Component one, Component two){}
		};
	}
	
	/**
	 * @author  Administrator
	 */
	public static abstract class HorizontalAlignments{
		

		//Aligns two components on the first components right edge.
		/**
		 * @uml.property  name="rIGHT_ALIGN"
		 * @uml.associationEnd  
		 */
		private static final Alignment RIGHT_ALIGN = new Alignment(){
			@Override
			public void align(Component one, Component two){
				two.setLocation(one.getX() + (one.getWidth() - two.getWidth()), two.getY());
			}
		};

		//Aligns two components on the first components left edge.
		/**
		 * @uml.property  name="lEFT_ALIGN"
		 * @uml.associationEnd  
		 */
		private static final Alignment LEFT_ALIGN = new Alignment(){
			@Override
			public void align(Component one, Component two){
				two.setLocation(one.getX(), two.getY());
			}
		};
		//Makes the center of the second component equal to the center of the first
		/**
		 * @uml.property  name="cENTER_ALIGN"
		 * @uml.associationEnd  
		 */
		private static final Alignment CENTER_ALIGN = new Alignment(){
			@Override
			public void align(Component one, Component two){
				two.setLocation(one.getX() + (one.getWidth() / 2 - two.getWidth()/ 2), two.getY());
			}
		};
	}
	
	/**
	 * @author   Administrator
	 */
	public static enum HorizontalAlign implements Alignment{
		
		/**
		 * @uml.property  name="nONE"
		 * @uml.associationEnd  
		 */
		NONE(DefaultAlignments.PASS_THROUGH_ALIGN), 
		/**
		 * @uml.property  name="rIGHT"
		 * @uml.associationEnd  
		 */
		RIGHT(HorizontalAlignments.RIGHT_ALIGN), 
		/**
		 * @uml.property  name="lEFT"
		 * @uml.associationEnd  
		 */
		LEFT(HorizontalAlignments.LEFT_ALIGN), 
		/**
		 * @uml.property  name="cENTER"
		 * @uml.associationEnd  
		 */
		CENTER(HorizontalAlignments.CENTER_ALIGN);
		
		/**
		 * @uml.property  name="specificAlignmentCase"
		 * @uml.associationEnd  
		 */
		private final Alignment specificAlignmentCase;
		private HorizontalAlign(Alignment specificAlignmentCase){
			this.specificAlignmentCase = specificAlignmentCase;
		}
		public void align(Component one, Component two){
			specificAlignmentCase.align(one, two);
		}
		
	}
	
	/**
	 * @author  Administrator
	 */
	public static abstract class VerticalAlignments {
		//Aligns two components on the first components bottom edge.

		/**
		 * @uml.property  name="bOTTOM_ALIGN"
		 * @uml.associationEnd  
		 */
		private static final Alignment BOTTOM_ALIGN = new Alignment(){
			@Override
			public void align(Component one, Component two){
				two.setLocation(two.getX(), one.getY() + (one.getHeight() - two.getHeight()));
			}
		};

		//Aligns two components on the first components top edge.

		/**
		 * @uml.property  name="tOP_ALIGN"
		 * @uml.associationEnd  
		 */
		private static final Alignment TOP_ALIGN = new Alignment(){
			@Override
			public void align(Component one, Component two){
				two.setLocation(two.getX(), one.getY());
			}
		};

		//Aligns the middle of the second components bounds rectangle (y-axis) with the middle of the 
		//first components bounds rectangle
		/**
		 * @uml.property  name="mIDDLE_ALIGN"
		 * @uml.associationEnd  
		 */
		private static final Alignment MIDDLE_ALIGN = new Alignment(){
			@Override
			public void align(Component one, Component two){
				two.setLocation(two.getX(), one.getY() + (one.getHeight() / 2 - two.getHeight()/ 2));
			}
		};
	}
	
	/**
	 * Static enum for vertical alignment
	 * @author   Administrator
	 */
	public static enum VerticalAlign implements Alignment{
		/**
		 * @uml.property  name="nONE"
		 * @uml.associationEnd  
		 */
		NONE(DefaultAlignments.PASS_THROUGH_ALIGN), 
		/**
		 * @uml.property  name="tOP"
		 * @uml.associationEnd  
		 */
		TOP(VerticalAlignments.TOP_ALIGN), 
		/**
		 * @uml.property  name="mIDDLE"
		 * @uml.associationEnd  
		 */
		MIDDLE(VerticalAlignments.MIDDLE_ALIGN), 
		/**
		 * @uml.property  name="bOTTOM"
		 * @uml.associationEnd  
		 */
		BOTTOM(VerticalAlignments.BOTTOM_ALIGN);
		/**
		 * @uml.property  name="specificAlignmentCase"
		 * @uml.associationEnd  
		 */
		private final Alignment specificAlignmentCase;
		private VerticalAlign(Alignment specificAlignmentCase){
			this.specificAlignmentCase = specificAlignmentCase;
		}
		public void align(Component one, Component two){
			specificAlignmentCase.align(one, two);
		}
	}
	
	/**
	 * enum for defining the offset of one component with another. Offsets are defined in terms of the second component being offset by a certain amount.
	 * @author   Administrator
	 */
	public enum Offset implements Alignment{
		//Offset uses the components slightly differently
		//The offset value only works on the second component
		//The offset value is handed to the align methods by 
		//way of the first method parameter.
		/**
		 * @uml.property  name="x"
		 * @uml.associationEnd  
		 */
		X(new Alignment(){
			@Override
			public void align(Component offset, Component two) {
				two.setLocation(two.getX() + offset.getX(), two.getY());
			}
		}), 
		/**
		 * @uml.property  name="y"
		 * @uml.associationEnd  
		 */
		Y(new Alignment(){
			@Override
			public void align(Component offset, Component two) {
				two.setLocation(two.getX(), two.getY() + offset.getY());
			}
		});
		int amount;
		/**
		 * @uml.property  name="alignment"
		 * @uml.associationEnd  
		 */
		final Alignment alignment;
		private Offset(Alignment alignment){
			this.alignment = alignment;
			this.amount = 0;
		}
		
		public Offset by(int amount){
			this.amount = amount;
			return this;
		}
		
		@Override
		public void align(Component one, Component two) {
			Component offset = new Component(){
				public int getX(){
					return Offset.this.amount;
				}
				public int getY(){
					return Offset.this.amount;
				}
			};
			alignment.align(one, two);
		}
	}
	
	
}



