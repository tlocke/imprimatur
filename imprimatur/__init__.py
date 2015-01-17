
from ._version import get_versions
__version__ = get_versions()['version']
del get_versions

from imprimatur.core import run

__all__ = [run]
