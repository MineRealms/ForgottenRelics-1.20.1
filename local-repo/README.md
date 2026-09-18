# local-repo

This directory is a small Maven repository used to resolve the Thaumcraft 4R
1.20.1 development jar, which is not published anywhere public.

The `thaumcraft-forge-0.1.0-20711.pom` is committed; the matching jar is
**not** (it is a third-party binary, excluded via `.gitignore`).

To make the project build, copy the jar from the provided dev bundle:

```
1.20.1-forge-20711.zip            -> thaumcraft-forge-4.2.3.5-1.20.1-port.0.1.0-20711.jar
```

to:

```
local-repo/dev/tc4port/thaumcraft-forge/0.1.0-20711/thaumcraft-forge-0.1.0-20711.jar
```

The other jars of the bundle (`forbidden-magic`, `tainted-magic`,
`thaumic-tinkerer`, `thaumic-energistics`) are optional companions, not build
dependencies of this project.
