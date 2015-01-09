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
@0
M=D          //RAM[0] record the fisrt address of SCREEN

///////////////////////////
(SCAN)
@KBD
D=M
@BLACK
D;JGT
@WHITE
D;JEQ

///////////////////////////
(BLACK)
@2
M=-1          //RAM[2] record the color
@CHANGE
0;JMP

(WHITE)
@2
M=0
@CHANGE
0;JMP

///////////////////////////
(CHANGE)
@2	//check color
D=M	
@0
A=M	
M=D	
@0
D=M+1	
@KBD
D=A-D	
@0
M=M+1	
A=M
@CHANGE
D;JGT	
@RESTART
0;JMP


