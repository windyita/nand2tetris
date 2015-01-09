// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Mult.asm

// Multiplies R0 and R1 and stores the result in R2.
// (R0, R1, R2 refer to RAM[0], RAM[1], and RAM[3], respectively.)

// Put your code here.

//r2=0;
//while(r0>0){
//	r2=r2+r1;
//	r0--;
//}
//return r2;


@0
D=A
@R2
M=D

@R1
D=M
@END
D;JEQ

(WHILE)
		@R0
		D=M
		@END
		D;JLE

		@R1
		D=M
		@R2
		M=M+D

		@1
		D=A
		@R0
		M=M-D

		@WHILE
		0;JEQ

(END)
		@END
		0;JEQ





