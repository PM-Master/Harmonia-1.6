package gov.nist.csd.pm.server.parser;

import java.util.Vector;

/**
 * @author  gavrila@nist.gov
 * @version  $Revision: 1.1 $, $Date: 2008/07/16 17:02:58 $
 * @since  1.5
 */
public class RuleSpec {
  /**
 * @uml.property  name="id"
 */
String id;
  /**
 * @uml.property  name="label"
 */
String label;
  /**
 * @uml.property  name="rank"
 */
int rank;
  /**
 * @uml.property  name="pattern"
 * @uml.associationEnd  
 */
PatternSpec pattern;
  /**
 * @uml.property  name="actions"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="gov.nist.csd.pm.server.parser.ActionSpec"
 */
Vector<ActionSpec> actions;

  public RuleSpec(String id) {
    this.id = id;
    actions = new Vector<ActionSpec>();
  }

  /**
 * @param label
 * @uml.property  name="label"
 */
public void setLabel(String label) {
    this.label = label;
  }

  /**
 * @param rank
 * @uml.property  name="rank"
 */
public void setRank(int rank) {
    this.rank = rank;
  }

  /**
 * @param pattern
 * @uml.property  name="pattern"
 */
public void setPattern(PatternSpec pattern) {
    this.pattern = pattern;
  }

  /**
 * @param actions
 * @uml.property  name="actions"
 */
public void setActions(Vector<ActionSpec> actions) {
    this.actions = actions;
  }

  /**
 * @return
 * @uml.property  name="id"
 */
public String getId() {
    return id;
  }

  /**
 * @return
 * @uml.property  name="label"
 */
public String getLabel() {
    return label;
  }

  /**
 * @return
 * @uml.property  name="rank"
 */
public int getRank() {
    return rank;
  }

  /**
 * @return
 * @uml.property  name="actions"
 */
public Vector<ActionSpec> getActions() {
    return actions;
  }

  /**
 * @return
 * @uml.property  name="pattern"
 */
public PatternSpec getPattern() {
    return pattern;
  }
} 
