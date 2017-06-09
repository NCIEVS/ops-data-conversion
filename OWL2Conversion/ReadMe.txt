ReadMe.txt for

OWL1 to OWL2 Process
Last update: RWW 11/16/2016
====================

1. Export OWL1 from Protege 1.5.1
2. perl ConvertOWL2.pl Thesaurus-161031-16.10e.owl
3. Resulting output will be named Thesaurus-161031-16.10e.owl-Step12345.owl
4. Load this resulting file into Protege 5.1.0
5. 'Save As...' RDF/XML named "Thesaurus-161031-16.10e-OWL2.owl"
6. perl ConvertByCode.pl Thesaurus-161031-16.10e-OWL2.owl
7. Resulting output will be named Thesaurus-161031-16.10e-OWL2.owl-byCode.owl

KNOWN ISSUES
============

Transforming to Code...
WARNING: Unconverted class defaultLanguage
WARNING: Unconverted class range

Neither of these declarations have codes.