from imprimatur.api import run
from ._version import get_versions
__version__ = get_versions()['version']
del get_versions


__all__ = [run]
