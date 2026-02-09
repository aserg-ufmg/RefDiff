detected_schema = {
    "index": int,
    "url": str,
    "repository": str,
    "commit": str,
    "type": str,
    "before": str,
    "after": str,
}

oracle_java_schema = {
    "index": int,
    "Commit URL": str,
    "Refactoring Type": str,
    "Description": str,
    "Relationship Type": str,
    "CST Node before": str,
    "CST Node After": str,
    "Expected?": str,
    "RefDiff 2.0": str,
    "RefDiff 1.0": str,
    "RMiner": str,
    "Evaluators": str,
    "Evaluators' classification": str,
}

result_java_schema = {
    "commit url": str,
    "refactoring type": str,
    "relationship type": str,
    "before": str,
    "after": str,
    "expected": str,
    "baseline result": str,
    "detected result": str,
    "equal to baseline": bool,
    "oracle index": int,
    "detected index": int,
    "note": str,
}

oracle_c_precision_schema = {
    "index": int,
    "Repository": str,
    "Commit": str,
    "Type": str,
    "Node Before": str,
    "Node After": str,
    "Commit URL": str,
    "Result": str,
}

oracle_c_recall_schema = {
    "index": int,
    "Type": str,
    "Repository": str,
    "Commit": str,
    "Node Before": str,
    "Node After": str,
    "Detected": str,
}

result_c_schema = {
    "commit url": str,
    "type": str,
    "before": str,
    "after": str,
    "baseline result": str,
    "detected result": str,
    "equal to baseline": bool,
    "oracle index": int,
    "detected index": int,
    "note": str,
}

oracle_js_precision_schema = {
    "index": int,
    "Repository": str,
    "Commit": str,
    "Rel. Type": str,
    "Node type": str,
    "Location before": str,
    "Local name before": str,
    "Location after": str,
    "Local name after": str,
    "Commit URL": str,
    "Result": str,
    "Comments": str,
}

oracle_js_recall_schema = {
    "index": int,
    "Commit URL": str,
    "Rel. Type": str,
    "Node before": str,
    "Node After": str,
}

result_js_schema = {
    "commit url": str,
    "type": str,
    "before": str,
    "after": str,
    "baseline result": str,
    "detected result": str,
    "equal to baseline": bool,
    "oracle index": int,
    "detected index": int,
    "note": str,
}
