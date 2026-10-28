import pytest

from src.load_to_mysql import optional_int


def test_optional_int_converts_valid_number():
    assert optional_int("42") == 42


def test_optional_int_returns_none_for_empty_values():
    assert optional_int(None) is None
    assert optional_int("") is None
    assert optional_int("   ") is None


def test_optional_int_rejects_invalid_number():
    with pytest.raises(ValueError):
        optional_int("abc")