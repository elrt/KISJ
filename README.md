# KISJ
Keep It Simple Java!
*(Because why interpret line-by-line with fseek when you can unleash the full wrath of JVM bytecode?)*

KISJ is an over-engineered, Ahead-Of-Time (AOT) compiler for the KISS (Keep It Simple Syntax) esoteric language.

In KISS, there are no objects, no 26 local variables, and certainly no sanity - there is only the sacred integer register x, worshipped like an ancient deity. But while traditional esolang interpreters leisurely reread files off your hard drive, KISJ takes your single-character incantations and compiles them straight into ultra super mega blazing fast native JVM bytecode (.class) via OW2 ASM!!!

### Prerequisites
* JDK 8 or higher
* Apache Maven


Installation
```bash
git clone https://github.com/elliktronic/KISJ.git
cd KISJ
```
Usage
```bash
#compile src
java -jar target/KISJ-1.0-SNAPSHOT.jar program.kiss
#run program
java Program
```

***NOW X IS YOUR LIFE***
