import unittest

from pandas import DataFrame, testing

from src.join_c import format_detected_before_after, format_oracle_before_after, join_table_c_precision, format_type, format_detected_before_after

class TestJoinC(unittest.TestCase):
    def test_format_type(self):
        self.assertEqual(
            "RENAME_FILE", format_type("RENAME", "{File oldName at foo.c:1}")
        )
        self.assertEqual(
            "MOVE_FUNCTION", format_type("MOVE", "{Function oldName at foo.c:1}")
        )
        self.assertEqual(
            "EXTRACT", format_type("EXTRACT", "{Function oldName at foo.c:1}")
        )

    def test_format_detected_before_after(self):
        self.assertEqual(
            "src/foo.c:bar",
            format_detected_before_after("{Function bar(baz, qux) at src/foo.c:123})"),
        )
        self.assertEqual(
            "arch/arm/plat-omap/dmtimer.c",
            format_detected_before_after("{File dmtimer.c at arch/arm/plat-omap/dmtimer.c:1}"),
        )
    def test_format_oracle_before_after(self):
        self.assertEqual(
            "src/network/Server.c:swServer_call_hook_func",
            format_oracle_before_after(
                "src/network/Server.c:swServer_call_hook_func(swServer, swServer_hook_type):30696-30997", "MOVE_FUNCTION"
            ),
        )
        self.assertEqual(
            "ext/mbstring/mbstring.c:php_mb_chr",
            format_oracle_before_after(
                "ext/mbstring/mbstring.c:php_mb_chr(long, char)", "RENAME_FUNCTION"
            ),
        )
        self.assertEqual(
            "ext/mbstring/mbstring.c",
            format_oracle_before_after(
                "ext/mbstring/mbstring.c", "MOVE_RENAME_FILE"
            ),
        )



