def calc_precision(tp: float, fp: float) -> float:
    return float(tp) / (tp + fp) if (tp + fp) > 0 else 0.0

def calc_recall(tp: float, fn: float) -> float:
    return float(tp) / (tp + fn) if (tp + fn) > 0 else 0.0
