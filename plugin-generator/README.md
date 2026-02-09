# Generating a New Language Plugin
- Install Universal Ctags CLI
- Add tree sitter dependency to build.gradle
- Set Language
- Add sample file to context/{langage}/src/
- Run Plugin Maker with argument "{language}"
For example, to generate a plugin for C language, run:
```bash
./gradlew :plugin-generator:run --args="c"
```
This will create a AST 

```bash
./gradlew runPluginImprover
```

# Validation
- Make sure repo directory has been created like this: `plugin-generator/repo`

```bash
./gradlew runValidator -Plang=Java
```

# Extensions
`extensions-map.txt` is a output file of `ctags --list-maps` command that contains mapping from language to file extensions.
Define file extensions for a new language based on this file.
