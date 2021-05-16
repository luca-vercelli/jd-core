package jd.instruction.bytecode.factory;

import java.util.List;
import java.util.Stack;

import jd.classfile.ClassFile;
import jd.classfile.Method;
import jd.instruction.bytecode.ByteCodeConstants;
import jd.instruction.bytecode.instruction.ANewArray;
import jd.instruction.bytecode.instruction.Instruction;


public class ANewArrayFactory extends InstructionFactory
{
	public int create(
			ClassFile classFile, Method method, List<Instruction> list, 
			List<Instruction> listForAnalyze,
			Stack<Instruction> stack, byte[] code, int offset, 
			int lineNumber, boolean[] jumps)
	{
		final int opcode = code[offset] & 255;
		final int index = 
			((code[offset+1] & 255) << 8) | (code[offset+2] & 255);
		final Instruction instruction = 
			new ANewArray(opcode, offset, lineNumber, index, stack.pop());
		
		stack.push(instruction);
		listForAnalyze.add(instruction);
		
		return ByteCodeConstants.NO_OF_OPERANDS[opcode];
	}
}