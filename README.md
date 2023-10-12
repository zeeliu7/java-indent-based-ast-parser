A Java program to help breaking user code into blocks based on given indents, mark missing code elements (curly brackets, semicolons, etc), and dynamically change block contents based on user insertion and deletion at specific positions. Intended to use for abstract syntax tree parsing for real-time collaborative programming. \
The `overriding` branch will automatically add missing curly braces and semicolon for a piece of code. The `main` branch will re-parse related blocks under each user insertion and deletion.
