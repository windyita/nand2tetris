// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Fill.asm

// Runs an infinite loop that listens to the keyboard input. 
// When a key is pressed (any key), the program blackens the screen,
// i.e. writes "black" in every pixel. When no key is pressed, the
// program clears the screen, i.e. writes "white" in every pixel.

// Put your code here.
(RESTART)
@16384
D=A
@1          //START POINT
M=D

(KEYBRD)
@KBD
D=M
@WHITE
D;JEQ
@BLACK
D;JGT

(WHITE)
@2
M=0
@CHANGE
0;JMP
(BLACK)
@2
M=-1
@CHANGE
0;JMP

(CHANGE)
@1
D=M
@KBD
D=A-D
@RESTART
D;JLE
@2
D=M      //RECORD THE COLOR
@1
A=M
M=D
@1
M=M+1
@CHANGE
0;JMP


