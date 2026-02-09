# from math import nan
# import unittest

# from pandas import DataFrame, testing

# from src.join_java import join_table_java

# class TestJoin(unittest.TestCase):
#     def test_TP(self):
#         oracle_data = {
#             "index": [0],
#             "Commit URL": [f"https://github.com/owner/repo/commit/1"],
#             "Refactoring Type": [""],
#             "Description": [""],
#             "Relationship Type": ["EXTRACT"],
#             "CST Node before": ["{Method oldMethod() at foo.java:1}"],
#             "CST Node After": ["{Method newExtractedPart() at foo.java:1}"],
#             "Expected?": ["T"],
#             "RefDiff 2.0": [""],
#             "RefDiff 1.0": [""],
#             "RMiner": [""],
#             "Evaluators": [""],
#             "Evaluators' classification": [""],
#         }
#         detected_data = {
#             "index": [0],
#             "repository": ["repo"],
#             "commit": ["1"],
#             "type": ["EXTRACT"],
#             "before": ["{Method oldMethod() at foo.java:1}"],
#             "after": ["{Method newExtractedPart() at foo.java:1}"],
#         }
#         want_data = {
#             "commit url": [f"https://github.com/owner/repo/commit/1"],
#             "refactoring type": [""],
#             "relationship type": ["EXTRACT"],
#             "before": ["{Method oldMethod() at foo.java:1}"],
#             "after": ["{Method newExtractedPart() at foo.java:1}"],
#             "expected": ["T"],
#             "baseline result": [""],
#             "detected result": ["TP"],
#             "equal to baseline": False,
#             "oracle index": [0],
#             "detected index": [0],
#             "note": [""],
#         }

#         oracle_df = DataFrame(oracle_data)
#         detected_df = DataFrame(detected_data)

#         result_df = join_table_java(oracle_df, detected_df, "owner")

#         want_df = DataFrame(want_data)
#         testing.assert_frame_equal(result_df, want_df)

#     def test_TN(self):
#         oracle_data = {
#             "index": [0, 1],
#             "Commit URL": [f"https://github.com/owner/repo/commit/1", f"https://github.com/owner/repo/commit/2"],
#             "Refactoring Type": ["", ""],
#             "Description": ["", ""],
#             "Relationship Type": ["EXTRACT", "FOO"],
#             "CST Node before": ["{Method oldMethod() at foo.java:1}", "{Method doSomething() at foo.java:1}"],
#             "CST Node After": ["{Method newExtractedPart() at foo.java:1}", "{Method doSomethingElse() at foo.java:1}"],
#             "Expected?": ["T", "F"],
#             "RefDiff 2.0": ["", ""],
#             "RefDiff 1.0": ["", ""],
#             "RMiner": ["", ""],
#             "Evaluators": ["", ""],
#             "Evaluators' classification": ["", ""],
#         }
#         detected_data = {
#             "index": [0],
#             "repository": ["repo"],
#             "commit": ["1"],
#             "type": ["EXTRACT"],
#             "before": ["{Method oldMethod() at foo.java:1}"],
#             "after": ["{Method newExtractedPart() at foo.java:1}"],
#         }
#         want_data = {
#             "commit url": [f"https://github.com/owner/repo/commit/1", f"https://github.com/owner/repo/commit/2"],
#             "refactoring type": ["", ""],
#             "relationship type": ["EXTRACT", "FOO"],
#             "before": ["{Method oldMethod() at foo.java:1}", "{Method doSomething() at foo.java:1}"],
#             "after": ["{Method newExtractedPart() at foo.java:1}", "{Method doSomethingElse() at foo.java:1}"],
#             "expected": ["T", "F"],
#             "baseline result": ["", ""],
#             "detected result": ["TP", "TN"],
#             "equal to baseline": [False, False],
#             "oracle index": [0, 1],
#             "detected index": [0, nan],
#             "note": ["", ""],
#         }

#         oracle_df = DataFrame(oracle_data)
#         detected_df = DataFrame(detected_data)

#         result_df = join_table_java(oracle_df, detected_df, "owner")

#         want_df = DataFrame(want_data)
#         testing.assert_frame_equal(result_df, want_df)

#     def test_FP_in_oracle(self):
#         oracle_data = {
#             "index": [0, 1],
#             "Commit URL": [
#                 f"https://github.com/owner/repo/commit/1",
#                 f"https://github.com/owner/repo/commit/2",
#             ],
#             "Refactoring Type": ["", ""],
#             "Description": ["", ""],
#             "Relationship Type": ["EXTRACT", "FOO"],
#             "CST Node before": [
#                 "{Method oldMethod() at foo.java:1}",
#                 "{Method doSomething() at foo.java:1}",
#             ],
#             "CST Node After": [
#                 "{Method newExtractedPart() at foo.java:1}",
#                 "{Method doSomethingElse() at foo.java:1}",
#             ],
#             "Expected?": ["T", "F"],
#             "RefDiff 2.0": ["", ""],
#             "RefDiff 1.0": ["", ""],
#             "RMiner": ["", ""],
#             "Evaluators": ["", ""],
#             "Evaluators' classification": ["", ""],
#         }
#         detected_data = {
#             "index": [0, 1],
#             "repository": ["repo", "repo"],
#             "commit": ["1", "2"],
#             "type": ["EXTRACT", "FOO"],
#             "before": ["{Method oldMethod() at foo.java:1}", "{Method doSomething() at foo.java:1}"],
#             "after": ["{Method newExtractedPart() at foo.java:1}", "{Method doSomethingElse() at foo.java:1}"],
#         }
#         want_data = {
#             "commit url": [f"https://github.com/owner/repo/commit/1", f"https://github.com/owner/repo/commit/2"],
#             "refactoring type": ["", ""],
#             "relationship type": ["EXTRACT", "FOO"],
#             "before": ["{Method oldMethod() at foo.java:1}", "{Method doSomething() at foo.java:1}"],
#             "after": ["{Method newExtractedPart() at foo.java:1}", "{Method doSomethingElse() at foo.java:1}"],
#             "expected": ["T", "F"],
#             "baseline result": ["", ""],
#             "detected result": ["TP", "FP"],
#             "equal to baseline": [False, False],
#             "oracle index": [0, 1],
#             "detected index": [0, 1],
#             "note": ["", ""],
#         }

#         oracle_df = DataFrame(oracle_data)
#         detected_df = DataFrame(detected_data)

#         result_df = join_table_java(oracle_df, detected_df, "owner")

#         want_df = DataFrame(want_data)
#         testing.assert_frame_equal(result_df, want_df)

#     def test_FP_unexpected(self):
#         oracle_data = {
#             "index": [0],
#             "Commit URL": [f"https://github.com/owner/repo/commit/1"],
#             "Refactoring Type": [""],
#             "Description": [""],
#             "Relationship Type": ["EXTRACT"],
#             "CST Node before": ["{Method oldMethod() at foo.java:1}"],
#             "CST Node After": ["{Method newExtractedPart() at foo.java:1}"],
#             "Expected?": ["T"],
#             "RefDiff 2.0": [""],
#             "RefDiff 1.0": [""],
#             "RMiner": [""],
#             "Evaluators": [""],
#             "Evaluators' classification": [""],
#         }
#         detected_data = {
#             "index": [0, 1],
#             "repository": ["repo", "repo"],
#             "commit": ["1", "2"],
#             "type": ["EXTRACT", "FOO"],
#             "before": [
#                 "{Method oldMethod() at foo.java:1}",
#                 "{Method doSomething() at foo.java:1}",
#             ],
#             "after": [
#                 "{Method newExtractedPart() at foo.java:1}",
#                 "{Method doSomethingElse() at foo.java:1}",
#             ],
#         }
#         want_data = {
#             "commit url": [
#                 f"https://github.com/owner/repo/commit/1",
#                 f"https://github.com/owner/repo/commit/2",
#             ],
#             "refactoring type": ["", nan],
#             "relationship type": ["EXTRACT", "FOO"],
#             "before": [
#                 "{Method oldMethod() at foo.java:1}",
#                 "{Method doSomething() at foo.java:1}",
#             ],
#             "after": [
#                 "{Method newExtractedPart() at foo.java:1}",
#                 "{Method doSomethingElse() at foo.java:1}",
#             ],
#             "expected": ["T", nan],
#             "baseline result": ["", nan],
#             "detected result": ["TP", "FP"],
#             "equal to baseline": [False, False],
#             "oracle index": [0, nan],
#             "detected index": [0, 1],
#             "note": ["", ""],
#         }

#         oracle_df = DataFrame(oracle_data)
#         detected_df = DataFrame(detected_data)

#         result_df = join_table_java(oracle_df, detected_df, "owner")

#         want_df = DataFrame(want_data)
#         testing.assert_frame_equal(result_df, want_df)

#     def test_FN(self):
#         oracle_data = {
#             "index": [0, 1],
#             "Commit URL": [
#                 f"https://github.com/owner/repo/commit/1",
#                 f"https://github.com/owner/repo/commit/2",
#             ],
#             "Refactoring Type": ["", ""],
#             "Description": ["", ""],
#             "Relationship Type": ["EXTRACT", "FOO"],
#             "CST Node before": [
#                 "{Method oldMethod() at foo.java:1}",
#                 "{Method doSomething() at foo.java:1}",
#             ],
#             "CST Node After": [
#                 "{Method newExtractedPart() at foo.java:1}",
#                 "{Method doSomethingElse() at foo.java:1}",
#             ],
#             "Expected?": ["T", "T"],
#             "RefDiff 2.0": ["", ""],
#             "RefDiff 1.0": ["", ""],
#             "RMiner": ["", ""],
#             "Evaluators": ["", ""],
#             "Evaluators' classification": ["", ""],
#         }
#         detected_data = {
#             "index": [0],
#             "repository": ["repo"],
#             "commit": ["1"],
#             "type": ["EXTRACT"],
#             "before": ["{Method oldMethod() at foo.java:1}"],
#             "after": ["{Method newExtractedPart() at foo.java:1}"],
#         }
#         want_data = {
#             "commit url": [
#                 f"https://github.com/owner/repo/commit/1",
#                 f"https://github.com/owner/repo/commit/2",
#             ],
#             "refactoring type": ["", ""],
#             "relationship type": ["EXTRACT", "FOO"],
#             "before": [
#                 "{Method oldMethod() at foo.java:1}",
#                 "{Method doSomething() at foo.java:1}",
#             ],
#             "after": [
#                 "{Method newExtractedPart() at foo.java:1}",
#                 "{Method doSomethingElse() at foo.java:1}",
#             ],
#             "expected": ["T", "T"],
#             "baseline result": ["", ""],
#             "detected result": ["TP", "FN"],
#             "equal to baseline": [False, False],
#             "oracle index": [0, 1],
#             "detected index": [0, nan],
#             "note": ["", ""],
#         }

#         oracle_df = DataFrame(oracle_data)
#         detected_df = DataFrame(detected_data)

#         result_df = join_table_java(oracle_df, detected_df, "owner")

#         want_df = DataFrame(want_data)
#         testing.assert_frame_equal(result_df, want_df)

#     def test_ignore_line(self):
#         oracle_data = {
#             "index": [0],
#             "Commit URL": [f"https://github.com/owner/repo/commit/1"],
#             "Refactoring Type": [""],
#             "Description": [""],
#             "Relationship Type": ["EXTRACT"],
#             "CST Node before": ["{Method oldMethod() at foo.java:1}"],
#             "CST Node After": ["{Method newExtractedPart() at foo.java:1}"],
#             "Expected?": ["T"],
#             "RefDiff 2.0": [""],
#             "RefDiff 1.0": [""],
#             "RMiner": [""],
#             "Evaluators": [""],
#             "Evaluators' classification": [""],
#         }
#         detected_data = {
#             "index": [0],
#             "repository": ["repo"],
#             "commit": ["1"],
#             "type": ["EXTRACT"],
#             "before": ["{Method oldMethod() at foo.java:234}"],
#             "after": ["{Method newExtractedPart() at foo.java:567}"],
#         }
#         want_data = {
#             "commit url": [f"https://github.com/owner/repo/commit/1"],
#             "refactoring type": [""],
#             "relationship type": ["EXTRACT"],
#             "before": ["{Method oldMethod() at foo.java}"],
#             "after": ["{Method newExtractedPart() at foo.java}"],
#             "expected": ["T"],
#             "baseline result": [""],
#             "detected result": ["TP"],
#             "equal to baseline": False,
#             "oracle index": [0],
#             "detected index": [0],
#             "note": [""],
#         }

#         oracle_df = DataFrame(oracle_data)
#         detected_df = DataFrame(detected_data)

#         result_df = join_table_java(oracle_df, detected_df, "owner", does_ignore_line=True)

#         want_df = DataFrame(want_data)
#         testing.assert_frame_equal(result_df, want_df)
