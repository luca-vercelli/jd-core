package jd.core.process.analyzer.instruction.bytecode.factory;

import java.util.List;
import java.util.Stack;

import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.Method;
import jd.core.model.instruction.bytecode.ByteCodeConstants;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.LoadInstruction;


public class LLoadFactory extends InstructionFactory
{
	public int create(
			ClassFile classFile, Method method, List<Instruction> list, 
			List<Instruction> listForAnalyze,  
			Stack<Instruction> stack, byte[] code, int offset, 
			int lineNumber, boolean[] jumps)
	{
		final int opcode = code[offset] & 255;
		int index;
		
		if (opcode == ByteCodeConstants.LLOAD)
			index = code[offset+1] & 255;
		else
			index = opcode - ByteCodeConstants.LLOAD_0;
		
		final Instruction instruction = new LoadInstruction(
			ByteCodeConstants.LOAD, offset, lineNumber, index, "J");
			
		stack.push(instruction);
		listForAnalyze.add(instruction);

		return ByteCodeConstants.NO_OF_OPERANDS[opcode];
	}
}