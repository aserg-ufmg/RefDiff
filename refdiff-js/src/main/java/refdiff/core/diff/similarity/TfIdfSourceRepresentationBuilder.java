package refdiff.core.diff.similarity;

import java.util.List;

import refdiff.core.rast.RastNode;

public class TfIdfSourceRepresentationBuilder implements SourceRepresentationBuilder<TfIdfSourceRepresentation> {

    Vocabulary vocabulary = new Vocabulary();
    
    @Override
    public TfIdfSourceRepresentation buildForNode(RastNode node, List<String> tokenizedSourceCode) {
        Multiset<String> multiset = new Multiset<String>();
        multiset.addAll(tokenizedSourceCode);
        vocabulary.count(multiset.asSet());
        return new TfIdfSourceRepresentation(multiset, vocabulary);
    }

    @Override
    public TfIdfSourceRepresentation buildEmpty() {
        return new TfIdfSourceRepresentation(new Multiset<String>(), vocabulary);
    }

    @Override
    public TfIdfSourceRepresentation combine(TfIdfSourceRepresentation arg1, TfIdfSourceRepresentation arg2) {
        return arg1.combine(arg2);
    }

    @Override
    public TfIdfSourceRepresentation minus(TfIdfSourceRepresentation arg1, TfIdfSourceRepresentation arg2) {
        return arg1.minus(arg2);
    }

    @Override
    public double similarity(TfIdfSourceRepresentation arg1, TfIdfSourceRepresentation arg2) {
        return arg1.similarity(arg2);
    }

    @Override
    public double partialSimilarity(TfIdfSourceRepresentation arg1, TfIdfSourceRepresentation arg2) {
        return arg1.partialSimilarity(arg2);
    }

}
