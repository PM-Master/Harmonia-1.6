package gov.nist.csd.pm.application.medrec.editors;

import javax.swing.JFrame;

public abstract class Editor extends JFrame{
	
	public abstract void search();

	public abstract void setPropsWithAllFields(String first, String mi, String last, String ssn, String gen, String dob,
			String doc, String mrn, String date, String treat);

	public abstract void setStateSubmit(boolean b);

	public abstract void setStateSave(boolean b);
	
	public abstract ReadAllDocument buildReadAllDocument(ReadAllDocument reader);

}
