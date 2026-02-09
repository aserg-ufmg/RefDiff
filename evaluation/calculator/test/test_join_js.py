import unittest

from pandas import DataFrame, testing

from src.join_js import extract_identifier, extract_node_type, extract_file_path, format_before_after

class TestJoinJs(unittest.TestCase):
    def test_extract_node_type(self):
        self.assertEqual(
            "Function",
            extract_node_type("{Function bar at src/foo.js}"),
        )

    def test_extract_identifier(self):
        self.assertEqual(
            "bar",
            extract_identifier("{Function bar at src/foo.js}"),
        )

    def test_extract_file_path(self):
        self.assertEqual(
            "src/foo.js",
            extract_file_path("{Function bar at src/foo.js}"),
        )

    def test_format_before_after(self):
        self.assertEqual(
            'node("src/foo.js", "bar")',
            format_before_after("{Function bar at src/foo.js}"),
        )
