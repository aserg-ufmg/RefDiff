package tyRuBa.modes;

import tyRuBa.engine.PredicateIdentifier;

public interface PredInfoProvider {

	PredInfo getPredInfo(PredicateIdentifier predId) throws TypeModeError;
	PredInfo maybeGetPredInfo(PredicateIdentifier predId);
	
}