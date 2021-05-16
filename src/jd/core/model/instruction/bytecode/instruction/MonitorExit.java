package jd.core.model.instruction.bytecode.instruction;

import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.LocalVariables;

public class MonitorExit extends Instruction 
{
	public Instruction objectref;

	public MonitorExit(
		int opcode, int offset, int lineNumber, Instruction objectref)
	{
		super(opcode, offset, lineNumber);
		this.objectref = objectref;
	}

	public String getReturnedSignature(
			ConstantPool constants, LocalVariables localVariables) 
	{		
		return null;
	}
}