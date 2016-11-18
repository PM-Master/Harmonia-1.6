package gov.nist.csd.pm.common.pdf.support;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import gov.nist.csd.pm.common.action.ActionPublisher;
import gov.nist.csd.pm.common.action.ActionPublisherSupport;
import gov.nist.csd.pm.common.action.ActionRef;
import gov.nist.csd.pm.common.action.ActionSubscriber;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageNode;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.compose;
import static com.google.common.collect.Iterables.filter;
import static gov.nist.csd.pm.common.util.Functions2.nullFunction;

/**
 * Created by IntelliJ IDEA. User: Administrator Date: 1/24/11 Time: 1:57 PM A
 * utility class for generating Swing components based on the information
 * contained in a PDF AcroForm. This class makes it easy to generate a UI with
 * the appropriate interface elements in their specified places on the form.
 * 
 * the method buildFormUIForPage is one of the most important here.
 */
public class PDFFormUIBuilder implements ActionPublisher {

	/**
	 * @uml.property  name="_doc"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private final PDDocument _doc;
	/**
	 * @uml.property  name="aps"
	 * @uml.associationEnd  
	 */
	private final ActionPublisherSupport aps = new ActionPublisherSupport(this);

	/**
	 * Converts a Rectangle from PDF coordinates to AWT coordinates. The y-axis
	 * of one is inverted with respect to the other.
	 * 
	 * @param rect
	 *            - the PDF formatted rectangle
	 * @param mediaDim
	 *            - the Dimension of the page. Only the height is used in this
	 *            method.
	 * @return a Rectangle2D in the AWT coordinate system.
	 */
	private Rectangle2D toJavaRect(PDRectangle rect, Dimension mediaDim) {
		int width = (int) (difference(rect.getLowerLeftX(),
				rect.getUpperRightX()));
		int height = (int) (difference(rect.getLowerLeftY(),
				rect.getUpperRightY()));
		int xval = (int) rect.getLowerLeftX();
		int yval = (int) (mediaDim.getHeight() - height - rect.getLowerLeftY());
		return new java.awt.geom.Rectangle2D.Double(xval, yval, width, height);

	}

	/**
	 * Main constructor for the builder.
	 * 
	 * @param doc
	 *            - the document to build off of. This cannot be null.
	 */
	public PDFFormUIBuilder(PDDocument doc) {
		_doc = checkNotNull(doc);
	}

	/**
	 * Builds the UI for a specific page. An absolute layout is used. Any
	 * previous layout will be wiped out
	 * 
	 * @param pdfFormView
	 *            - the panel to build the UI into. This JPanel will end up
	 *            resized to the page dimensions * zoom
	 * @param page
	 *            - the page from which to pull AcroForm fields from.
	 * @param zoom
	 *            - an affine transform to apply to the components before
	 *            rendering. Only transforms and scaling apply correctly. Shears
	 *            and rotations will produce unintended effects.
	 * @return a map associating the PDField's in the page with the Components
	 *         created and inserted on the pdfFormView.
	 */
	public void buildFormUIForPage(JPanel pdfFormView, PDPage page,
			AffineTransform zoom) {
		pdfFormView.setLayout(null);
		int pageNum = getPageNumber(page);
		Iterable<PDField> fields = getIterableFieldsForPage(pageNum);
		int count = 0;
		Dimension mediaDim = page.findMediaBox().createDimension();
		System.out.println("Building UI; Media Dimension is: " + mediaDim);
		aps.removeAllActions();
		for (PDField field : fields) {
			count++;


				Component result = PDFieldSwingAdapters.getComponentForField(field);
				if (result != null) {
					result.setBackground(Color.lightGray);
					pdfFormView.add(result);
					setJComponentLocation(result, field, mediaDim, zoom);
				}


		}
		// System.out.printf("Placed %d fields\n", count);

	}

	/**
	 * Sets the location of a component to that of a field * transform
	 * 
	 * @param comp
	 *            - the component whose location will be set.
	 * @param field
	 *            - the field to get location information from.
	 * @param mediaDim
	 *            - the dimensions of the page.
	 * @param transform
	 *            - an additional transform to apply. Use an identity transform
	 *            for no additional transformation.
	 */
	private void setJComponentLocation(Component comp, PDField field,
			Dimension mediaDim, AffineTransform transform) {
		try {

			PDRectangle pdrect = PDFields.getPDRectangleForField(field);


			Rectangle2D rect = toJavaRect(pdrect, mediaDim);
			Shape result = transform.createTransformedShape(rect);
			rect = result.getBounds2D();
			comp.setSize((int) rect.getWidth(), (int) rect.getHeight());
			comp.setPreferredSize(comp.getSize());
			comp.setLocation((int) rect.getMinX(), (int) rect.getMinY());

			String message = String.format(
					"Setting component location for field: %s \n"
							+ "Location %s \n" + "Size: %s \n"
							+ "PreferredSize: %s \n",
					field.getFullyQualifiedName(), comp.getLocation(),
					comp.getSize(), comp.getPreferredSize());
			// Logger.getLogger(PDFFormUIBuilder.class.getName()).log(Level.INFO,
			// message);
		} catch (IOException ex) {
			Logger.getLogger(PDFFormUIBuilder.class.getName()).log(
					Level.SEVERE, null, ex);
		}
	}

	// Returns the absolute difference between lowval and highval
	private float difference(float one, float two) {
		return Math.abs(one - two);
	}

	/**
	 * Locates the PDField's found on a given page
	 * 
	 * @param pageNum
	 *            - the page number to get the fields of.
	 * @return a list of PDField's on that page.
	 */
	public List<PDField> getFieldsForPage(int pageNum) {
		return Lists.newArrayList(getIterableFieldsForPage(pageNum));
	}

	/**
	 * Locates the PDField's found on a given page as an Iterable.
	 * 
	 * @param pageNum
	 *            - the page number to get the fields of.
	 * @return a list of PDField's on that page.
	 */
	private Iterable<PDField> getIterableFieldsForPage(int pageNum) {
		PDAcroForm acroForm = _doc.getDocumentCatalog().getAcroForm();
		try {
			/*
			 * What this line states is: From all the fields of the form
			 * (acroForm.getFields()) return only the fields whose that satisfy
			 * the second argument. The second argument joins a function that
			 * finds the page number that a field is set on, with a predicate
			 * that compares that number with the number we are looking for
			 * (pageNum).
			 * 
			 * Read aloud it says From all fields, return only those fields that
			 * are attached to the given page number.
			 */
			return filter(
					acroForm.getFields(),
					compose(withPageNumber(pageNum),
							getFirstParentOfType(COSName.PAGE, PDPage.class)));
		} catch (IOException e) {
			e.printStackTrace(); // To change body of catch statement use File |
									// Settings | File Templates.
		}
		return Lists.newArrayList();
	}

	/**
	 * Gets the page number for a PDPage
	 * 
	 * @param page
	 * @return the 0-indexed page number.
	 */
	public static int getPageNumber(PDPage page) {
		checkNotNull(page);
		// System.out.println("Getting the parent of page " + page);
		PDPageNode pdPageNode = page.getParent();
		List<PDPage> pages = pdPageNode.getKids();
		return pages.indexOf(page);
	}

	private static Predicate<PDPage> withPageNumber(final int i) {
		return new Predicate<PDPage>() {

			@Override
			public boolean apply(PDPage pdPage) {

				return pdPage != null ? (getPageNumber(pdPage) == i) : false;

			}
		};
	}

	private static class FirstParentOfTypeFunction<T> implements
			Function<PDField, T> {

		private final COSName _name;
		private final Class<T> _asType;

		public FirstParentOfTypeFunction(COSName name, Class<T> asType) {
			_name = name;
			_asType = asType;
		}

		@Override
		public T apply(PDField pdField) {
			T returns = null;
			String typeName = _asType.getSimpleName();
			COSDictionary pageDictionary = pdField.getDictionary();
			// System.out.println("Getting the first parent of type " +
			// _asType.getSimpleName() + " for field " + pdField);
			do {
				COSBase base = pageDictionary.getDictionaryObject(
						COSName.PARENT, COSName.P);
				pageDictionary = base instanceof COSDictionary ? (COSDictionary) base
						: null;
			} while (pageDictionary != null
					&& !pageDictionary.getDictionaryObject(COSName.TYPE)
							.equals(_name));
			try {
				Constructor<T> typeCons = _asType
						.getConstructor(COSDictionary.class);
				if (isNotNull(pageDictionary)) {
					returns = typeCons.newInstance(pageDictionary);
				}
			} catch (NoSuchMethodException e) {
				throwConstructorException(typeName);
			} catch (InvocationTargetException e) {
				throwConstructorException(typeName);
			} catch (InstantiationException e) {
				throwConstructorException(typeName);
			} catch (IllegalAccessException e) {
				throwConstructorException(typeName);
			}
			return returns;
		}
	}

	public static <T> Function<PDField, T> getFirstParentOfType(
			final COSName name, final Class<T> asType) {
		final String typeName = asType.getSimpleName();
		try {
			if (isNotNull(asType.getConstructor(COSDictionary.class))) {
				return new FirstParentOfTypeFunction<T>(name, asType);
			}
		} catch (NoSuchMethodException e) {
			throwConstructorException(typeName);
		}
		return nullFunction();

	}

	private static boolean isNotNull(Object obj) {
		return obj != null;
	}

	private static void throwConstructorException(
			String attemptedInstantiationClassType) {
		throw new RuntimeException("cannot accept type "
				+ attemptedInstantiationClassType
				+ " as it lacks a constructor accepting COSDictionary");
	}

	@Override
	public List<ActionRef> publishedActions() {
		return aps.publishedActions();
	}

	@Override
	public void registerSubscriber(ActionSubscriber subscriber) {
		aps.registerSubscriber(subscriber);
	}

	@Override
	public void removeSubscriber(ActionSubscriber subscriber) {
		aps.removeSubscriber(subscriber);
	}
}
