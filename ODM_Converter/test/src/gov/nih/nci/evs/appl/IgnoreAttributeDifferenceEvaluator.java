package gov.nih.nci.evs.appl;

import org.w3c.dom.Attr;
import org.w3c.dom.Node;
import org.xmlunit.diff.Comparison;
import org.xmlunit.diff.ComparisonResult;
import org.xmlunit.diff.DifferenceEvaluator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IgnoreAttributeDifferenceEvaluator implements DifferenceEvaluator {
  private final Map<String, List<String>> attributeNames;

  public IgnoreAttributeDifferenceEvaluator(Map<String, List<String>> attributeNames) {
    this.attributeNames = attributeNames != null ? attributeNames : new HashMap<>();
  }

  @Override
  public ComparisonResult evaluate(Comparison comparison, ComparisonResult outcome) {
    if (outcome == ComparisonResult.EQUAL) return outcome;
    final Node controlNode = comparison.getControlDetails().getTarget();
    if (controlNode instanceof Attr) {
      String parentName = ((Attr) controlNode).getOwnerElement().getLocalName();
      List<String> ignoredAttributes = attributeNames.get(parentName);
      Attr attr = (Attr) controlNode;
      if (ignoredAttributes != null && ignoredAttributes.contains(attr.getName())) {
        return ComparisonResult.SIMILAR;
      }
    }
    return outcome;
  }
}
