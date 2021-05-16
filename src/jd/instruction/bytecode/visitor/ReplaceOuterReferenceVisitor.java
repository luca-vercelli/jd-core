package jd.instruction.bytecode.visitor;

import java.util.List;

import jd.instruction.bytecode.ByteCodeConstants;
import jd.instruction.bytecode.instruction.ANewArray;
import jd.instruction.bytecode.instruction.AThrow;
import jd.instruction.bytecode.instruction.ArrayLength;
import jd.instruction.bytecode.instruction.ArrayLoadInstruction;
import jd.instruction.bytecode.instruction.ArrayStoreInstruction;
import jd.instruction.bytecode.instruction.AssertInstruction;
import jd.instruction.bytecode.instruction.AssignmentInstruction;
import jd.instruction.bytecode.instruction.BinaryOperatorInstruction;
import jd.instruction.bytecode.instruction.CheckCast;
import jd.instruction.bytecode.instruction.ComplexConditionalBranchInstruction;
import jd.instruction.bytecode.instruction.ConvertInstruction;
import jd.instruction.bytecode.instruction.DupStore;
import jd.instruction.bytecode.instruction.GetField;
import jd.instruction.bytecode.instruction.GetStatic;
import jd.instruction.bytecode.instruction.IfCmp;
import jd.instruction.bytecode.instruction.IfInstruction;
import jd.instruction.bytecode.instruction.IncInstruction;
import jd.instruction.bytecode.instruction.IndexInstruction;
import jd.instruction.bytecode.instruction.InitArrayInstruction;
import jd.instruction.bytecode.instruction.InstanceOf;
import jd.instruction.bytecode.instruction.Instruction;
import jd.instruction.bytecode.instruction.InvokeInstruction;
import jd.instruction.bytecode.instruction.InvokeNew;
import jd.instruction.bytecode.instruction.InvokeNoStaticInstruction;
import jd.instruction.bytecode.instruction.LookupSwitch;
import jd.instruction.bytecode.instruction.MonitorEnter;
import jd.instruction.bytecode.instruction.MonitorExit;
import jd.instruction.bytecode.instruction.MultiANewArray;
import jd.instruction.bytecode.instruction.NewArray;
import jd.instruction.bytecode.instruction.Pop;
import jd.instruction.bytecode.instruction.PutField;
import jd.instruction.bytecode.instruction.PutStatic;
import jd.instruction.bytecode.instruction.ReturnInstruction;
import jd.instruction.bytecode.instruction.StoreInstruction;
import jd.instruction.bytecode.instruction.TableSwitch;
import jd.instruction.bytecode.instruction.TernaryOpStore;
import jd.instruction.bytecode.instruction.TernaryOperator;
import jd.instruction.bytecode.instruction.UnaryOperatorInstruction;

/*
 * Replace 'ALoad(1)' in constructor by 'OuterThis()':
 * replace '???.xxx' by 'TestInnerClass.this.xxx'.
 */
public class ReplaceOuterReferenceVisitor 
{
	private int opcode;
	private int index;
	private int outerThisInstructionIndex;
	
	public ReplaceOuterReferenceVisitor(
		int opcode, int index, int outerThisInstructionIndex)
	{
		this.opcode = opcode;
		this.index = index;
		this.outerThisInstructionIndex = outerThisInstructionIndex;
	}
	
	public void init(int opcode, int index)
	{
		this.opcode = opcode;
		this.index = index;
	}
	
	public void visit(Instruction instruction)
	{
		switch (instruction.opcode)
		{
		case ByteCodeConstants.ARRAYLENGTH:
			{
				ArrayLength al = (ArrayLength)instruction;
				if (match(al.arrayref))
					al.arrayref = newInstruction(al.arrayref);
				else
					visit(al.arrayref);
			}
			break;
		case ByteCodeConstants.AASTORE:
		case ByteCodeConstants.ARRAYSTORE:
			{
				ArrayStoreInstruction asi = (ArrayStoreInstruction)instruction;
				if (match(asi.arrayref))
					asi.arrayref = newInstruction(asi.arrayref);
				else
					visit(asi.arrayref);
				if (match(asi.indexref))
					asi.indexref = newInstruction(asi.indexref);
				else
					visit(asi.indexref);
				if (match(asi.valueref))
					asi.valueref = newInstruction(asi.valueref);
				else
					visit(asi.valueref);
			}
			break;
		case ByteCodeConstants.ASSERT:
			{
				AssertInstruction ai = (AssertInstruction)instruction;
				if (match(ai.test))
					ai.test = newInstruction(ai.test);
				else
					visit(ai.test);
				if (ai.msg != null)
				{
					if (match(ai.msg))
						ai.msg = newInstruction(ai.msg);
					else
						visit(ai.msg);
				}
			}
			break;
		case ByteCodeConstants.ATHROW:
			{
				AThrow aThrow = (AThrow)instruction;
				if (match(aThrow.value))
					aThrow.value = newInstruction(aThrow.value);
				else
					visit(aThrow.value);
			}
			break;
		case ByteCodeConstants.UNARYOP:
			{
				UnaryOperatorInstruction uoi = (UnaryOperatorInstruction)instruction;
				if (match(uoi.value))
					uoi.value = newInstruction(uoi.value);
				else
					visit(uoi.value);
			}
			break;
		case ByteCodeConstants.BINARYOP:
			{
				BinaryOperatorInstruction boi = (BinaryOperatorInstruction)instruction;
				if (match(boi.value1))
					boi.value1 = newInstruction(boi.value1);
				else
					visit(boi.value1);
				if (match(boi.value2))
					boi.value2 = newInstruction(boi.value2);
				else
					visit(boi.value2);
			}
			break;
		case ByteCodeConstants.CHECKCAST:
			{
				CheckCast checkCast = (CheckCast)instruction;
				if (match(checkCast.objectref))
					checkCast.objectref = newInstruction(checkCast.objectref);
				else
					visit(checkCast.objectref);
			}
			break;
		case ByteCodeConstants.STORE:
		case ByteCodeConstants.ASTORE:
		case ByteCodeConstants.ISTORE:
			{
				StoreInstruction storeInstruction = (StoreInstruction)instruction;
				if (match(storeInstruction.valueref))
					storeInstruction.valueref = newInstruction(storeInstruction.valueref);
				else
					visit(storeInstruction.valueref);
			}
			break;
		case ByteCodeConstants.DUPSTORE:
			{
				DupStore dupStore = (DupStore)instruction;
				if (match(dupStore.objectref))
					dupStore.objectref = newInstruction(dupStore.objectref);
				else
					visit(dupStore.objectref);
			}
			break;
		case ByteCodeConstants.CONVERT:
			{
				ConvertInstruction ci = (ConvertInstruction)instruction;
				if (match(ci.value))
					ci.value = newInstruction(ci.value);
				else
					visit(ci.value);
			}
			break;
		case ByteCodeConstants.IFCMP:
			{
				IfCmp ifCmp = (IfCmp)instruction;
				if (match(ifCmp.value1))
					ifCmp.value1 = newInstruction(ifCmp.value1);
				else
					visit(ifCmp.value1);
				if (match(ifCmp.value2))
					ifCmp.value2 = newInstruction(ifCmp.value2);
				else
					visit(ifCmp.value2);
			}
			break;
		case ByteCodeConstants.IF:
		case ByteCodeConstants.IFXNULL:
			{
				IfInstruction iff = (IfInstruction)instruction;
				if (match(iff.value))
					iff.value = newInstruction(iff.value);
				else
					visit(iff.value);
			}
			break;			
		case ByteCodeConstants.COMPLEXIF:
			{
				List<Instruction> branchList = 
					((ComplexConditionalBranchInstruction)instruction).instructions;
				for (int i=branchList.size()-1; i>=0; --i)
					visit(branchList.get(i));
			}
			break;
		case ByteCodeConstants.INSTANCEOF:
			{
				InstanceOf instanceOf = (InstanceOf)instruction;
				if (match(instanceOf.objectref))
					instanceOf.objectref = newInstruction(instanceOf.objectref);
				else
					visit(instanceOf.objectref);
			}
			break;
		case ByteCodeConstants.INVOKEINTERFACE:
		case ByteCodeConstants.INVOKESPECIAL:
		case ByteCodeConstants.INVOKEVIRTUAL:
			{
				InvokeNoStaticInstruction insi = 
					(InvokeNoStaticInstruction)instruction;
				if (match(insi.objectref))
					insi.objectref = newInstruction(insi.objectref);
				else
					visit(insi.objectref);
			}
		case ByteCodeConstants.INVOKESTATIC:
			{
				List<Instruction> list = ((InvokeInstruction)instruction).args;
				for (int i=list.size()-1; i>=0; --i)
				{
					if (match(list.get(i)))
						list.set(i, newInstruction(list.get(i)));
					else
						visit(list.get(i));
				}
			}
			break;
		case ByteCodeConstants.INVOKENEW:
			{
				List<Instruction> list = ((InvokeNew)instruction).args;
				for (int i=list.size()-1; i>=0; --i)
				{
					if (match(list.get(i)))
						list.set(i, newInstruction(list.get(i)));
					else
						visit(list.get(i));
				}
			}
			break;
		case ByteCodeConstants.LOOKUPSWITCH:
			{
				LookupSwitch ls = (LookupSwitch)instruction;
				if (match(ls.key))
					ls.key = newInstruction(ls.key);
				else
					visit(ls.key);
			}
			break;			
		case ByteCodeConstants.MONITORENTER:
			{
				MonitorEnter monitorEnter = (MonitorEnter)instruction;
				if (match(monitorEnter.objectref))
					monitorEnter.objectref = newInstruction(monitorEnter.objectref);
				else
					visit(monitorEnter.objectref);
			}
			break;
		case ByteCodeConstants.MONITOREXIT:
			{
				MonitorExit monitorExit = (MonitorExit)instruction;
				if (match(monitorExit.objectref))
					monitorExit.objectref = newInstruction(monitorExit.objectref);
				else
					visit(monitorExit.objectref);
			}
			break;
		case ByteCodeConstants.MULTIANEWARRAY:
			{
				Instruction[] dimensions = ((MultiANewArray)instruction).dimensions;
				for (int i=dimensions.length-1; i>=0; --i)
				{
					if (match(dimensions[i]))
						dimensions[i] = newInstruction(dimensions[i]);
					else
						visit(dimensions[i]);
				}
			}
			break;
		case ByteCodeConstants.NEWARRAY:
			{
				NewArray newArray = (NewArray)instruction;
				if (match(newArray.dimension))
					newArray.dimension = newInstruction(newArray.dimension);
				else
					visit(newArray.dimension);
			}
			break;
		case ByteCodeConstants.ANEWARRAY:
			{
				ANewArray aNewArray = (ANewArray)instruction;
				if (match(aNewArray.dimension))
					aNewArray.dimension = newInstruction(aNewArray.dimension);
				else
					visit(aNewArray.dimension);
			}
			break;
		case ByteCodeConstants.POP:
			{
				Pop pop = (Pop)instruction;
				if (match(pop.objectref))
					pop.objectref = newInstruction(pop.objectref);
				else
					visit(pop.objectref);
			}
			break;
		case ByteCodeConstants.PUTFIELD:
			{
				PutField putField = (PutField)instruction;
				if (match(putField.objectref))
					putField.objectref = newInstruction(putField.objectref);
				else
					visit(putField.objectref);
				if (match(putField.valueref))
					putField.valueref = newInstruction(putField.valueref);
				else
					visit(putField.valueref);
			}
			break;
		case ByteCodeConstants.PUTSTATIC:
			{
				PutStatic putStatic = (PutStatic)instruction;
				if (match(putStatic.valueref))
					putStatic.valueref = newInstruction(putStatic.valueref);
				else
					visit(putStatic.valueref);
			}
			break;
		case ByteCodeConstants.XRETURN:
			{
				ReturnInstruction ri = (ReturnInstruction)instruction;
				if (match(ri.valueref))
					ri.valueref = newInstruction(ri.valueref);
				else
					visit(ri.valueref);
			}
			break;			
		case ByteCodeConstants.TABLESWITCH:
			{
				TableSwitch ts = (TableSwitch)instruction;
				if (match(ts.key))
					ts.key = newInstruction(ts.key);
				else
					visit(ts.key);
			}
			break;			
		case ByteCodeConstants.TERNARYOPSTORE:
			{
				TernaryOpStore tos = (TernaryOpStore)instruction;
				if (match(tos.objectref))
					tos.objectref = newInstruction(tos.objectref);
				else
					visit(tos.objectref);
			}
			break;		
		case ByteCodeConstants.TERNARYOP:	
			{
				TernaryOperator to = (TernaryOperator)instruction;
				if (match(to.test))
					to.test = newInstruction(to.test);
				else
					visit(to.test);
				if (match(to.value1))
					to.value1 = newInstruction(to.value1);
				else
					visit(to.value1);
				if (match(to.value2))
					to.value2 = newInstruction(to.value2);
				else
					visit(to.value2);
			}
			break;	
		case ByteCodeConstants.ASSIGNMENT:
			{
				AssignmentInstruction ai = (AssignmentInstruction)instruction;
				if (match(ai.value1))
					ai.value1 = newInstruction(ai.value1);
				else
					visit(ai.value1);
				if (match(ai.value2))
					ai.value2 = newInstruction(ai.value2);
				else
					visit(ai.value2);
			}
			break;
		case ByteCodeConstants.ARRAYLOAD:
			{
				ArrayLoadInstruction ali = (ArrayLoadInstruction)instruction;
				if (match(ali.arrayref))
					ali.arrayref = newInstruction(ali.arrayref);
				else
					visit(ali.arrayref);
				if (match(ali.indexref))
					ali.indexref = newInstruction(ali.indexref);
				else
					visit(ali.indexref);
			}
			break;
		case ByteCodeConstants.PREINC:			
		case ByteCodeConstants.POSTINC:		
			{
				IncInstruction ii = (IncInstruction)instruction;
				if (match(ii.value))
					ii.value = newInstruction(ii.value);
				else
					visit(ii.value);
			}
			break;
		case ByteCodeConstants.GETFIELD:
			{
				GetField gf = (GetField)instruction;
				if (match(gf.objectref))
					gf.objectref = newInstruction(gf.objectref);
				else
					visit(gf.objectref);
			}
			break;
		case ByteCodeConstants.INITARRAY:
		case ByteCodeConstants.NEWANDINITARRAY:
			{
				InitArrayInstruction iai = (InitArrayInstruction)instruction;
				if (match(iai.newArray))
					iai.newArray = newInstruction(iai.newArray);
				else
					visit(iai.newArray);
				if (iai.values != null)
					visit(iai.values);
			}
			break;
		case ByteCodeConstants.ACONST_NULL:
		case ByteCodeConstants.LOAD:
		case ByteCodeConstants.ALOAD:
		case ByteCodeConstants.ILOAD:
		case ByteCodeConstants.BIPUSH:
		case ByteCodeConstants.ICONST:
		case ByteCodeConstants.LCONST:
		case ByteCodeConstants.FCONST:
		case ByteCodeConstants.DCONST:
		case ByteCodeConstants.DUPLOAD:
		case ByteCodeConstants.GETSTATIC:
		case ByteCodeConstants.OUTERTHIS:
		case ByteCodeConstants.GOTO:
		case ByteCodeConstants.IINC:			
		case ByteCodeConstants.JSR:			
		case ByteCodeConstants.LDC:
		case ByteCodeConstants.LDC2_W:
		case ByteCodeConstants.NEW:
		case ByteCodeConstants.NOP:
		case ByteCodeConstants.SIPUSH:
		case ByteCodeConstants.RET:
		case ByteCodeConstants.RETURN:
		case ByteCodeConstants.EXCEPTIONLOAD:
		case ByteCodeConstants.RETURNADDRESSLOAD:
			break;
		default:
			System.err.println(
					"Can not replace DupLoad in " + 
					instruction.getClass().getName() + 
					", opcode=" + instruction.opcode);
		}
	}
	
	public void visit(List<Instruction> instructions)
	{
		for (int index=instructions.size()-1; index>=0; --index)
		{
			Instruction i = instructions.get(index);
			
			if (match(i))
				instructions.set(index, newInstruction(i));
			else
				visit(i);
		}
	}

	private boolean match(Instruction i)
	{
		return 
			(i.opcode == this.opcode) && 
			(((IndexInstruction)i).index == this.index);
	}
	
	private Instruction newInstruction(Instruction i)
	{
		return new GetStatic(
			ByteCodeConstants.OUTERTHIS, i.offset, 
			i.lineNumber, this.outerThisInstructionIndex);
	}
}