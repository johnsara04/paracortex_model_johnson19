package lN_simple3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
//import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.graph.Network;
import repast.simphony.space.grid.Grid;
import repast.simphony.util.ContextUtils;


public class UncognateCell extends Tcell {
// no extra fields
	
	//constructor
	public UncognateCell(
			//ContinuousSpace<Object>space,
			Grid<Object>grid,
			
			int retainTime,
			int age,
	        int timeSinceLastBind,
	        int timeSinceEntered,
	        int DCContacted,
	        double S1P1,
	        int istring,
	       boolean trackingvalue
	        )
	{
		super (
				//space,
				grid,
				
				retainTime,
				age,
				timeSinceLastBind,
				timeSinceEntered,
				DCContacted,
				S1P1,
				istring,
				trackingvalue
				);
	}
	

	
//no extra methods
	
	@Override
	 public void updateInitalS1P1 ()
	{
		setS1P1(1);
	}
	
	 @Override
	 public void seeIfExit() throws IOException
	    {
		 Parameters params = RunEnvironment.getInstance().getParameters();
	    	double value = RandomHelper.nextDoubleFromTo(0, 100000)/100000;//Math.random();// returns a random number between 0.0 and 0.999
	 
	    	double exitProb = (Double)params.getValue("Pe")* getS1P1(); //*Cs value  Pe = 0.009
	    	//System.out.println("exit prob = " + exitProb);
	    	if (value < exitProb)	
	    		//no need to remove links as it is already not bound
	    	{
	    		//int temp =lymph_node3DContext.getCellCount() -1;
				//lymph_node3DContext.setCellCount(temp);
	    	lymph_node3DContext.removeCellExit(this);}
	    	
	    	
	    	else
	    		checkAge();
	    	
	    }
	 
	 @Override
	 public void seeIfExit2() throws IOException
	    {
		 Parameters params = RunEnvironment.getInstance().getParameters();
	    	double value = RandomHelper.nextDoubleFromTo(0, 100000)/100000;//Math.random();// returns a random number between 0.0 and 0.999
	 
	    	double exitProb = (Double)params.getValue("Pe2")* getS1P1(); //*Cs value  Pe = 0.009
	    	//System.out.println("exit prob = " + exitProb);
	    	if (value < exitProb)	
	    		//no need to remove links as it is already not bound
	    	{
	    		//int temp =lymph_node3DContext.getCellCount() -1;
				//lymph_node3DContext.setCellCount(temp);
	    	lymph_node3DContext.removeCellExit(this);}
	    	
	    	
	    	else
	    		checkAge();
	    	
	    }
	 
	 
	 
	 
	 @Override
	public void checkAge()
	{
		int average= Constants.lifespanT;
		int n =  RandomHelper.createNormal(average,5000).nextInt();
		if (getAge()> n)
		{
			//need to access all nodes and remove bonds
			
			Context<Object> context = (Context)ContextUtils.getContext(this);
			Network<Object>net=(Network<Object>)context.getProjection("DC interaction network");
			List<Object> listNodes = new ArrayList();
			for (Object node: net.getAdjacent(this))
			{
				listNodes.add(node);
			}
			//set each node free
			for (Object Node : listNodes)
			{  
				if (Node instanceof DC )
				{
					int n2 = ((DC)Node).getBoundCount()-1;
					((DC)Node).setBoundCount(n2);
					int T = ((DC)Node).getTcellsContacted() ;
					if(T > 0){((DC)Node).setTcellsContacted((T+1));} //(was so you only track newly entered cells
					net.removeEdge(net.getEdge(this,Node));
				}	
			}			
			
			//int temp =lymph_node3DContext.getCellCount() -1;
			//lymph_node3DContext.setCellCount(temp);
			lymph_node3DContext.removeCellAgeing(this);
		}
	}
		@Override
		public void checkInflammation()
		{
			 if (timeSinceEntered == 0 || timeSinceEntered > Constants.responsetimeInflam)
			   
				//added a step to down regulate S1P1 , the threshold reach about half of the max stimuli. 
			 { 	Parameters params = RunEnvironment.getInstance().getParameters();
			  		double S1P1all_inflam = (Double)params.getValue("S1P1all_inflam");		
					double divide = (1.0-S1P1all_inflam) / 3.0;
					double a = S1P1all_inflam+divide;
					double b =S1P1all_inflam+divide+divide;			
					if (lymph_node3DContext.getTotalMHCII()<= 60000)
					{setS1P1(1);}
					
					if (lymph_node3DContext.getTotalMHCII()> 60000 && lymph_node3DContext.getTotalMHCII() <= 140000 )
						{ setS1P1(b);};
					if (lymph_node3DContext.getTotalMHCII()> 140000 && lymph_node3DContext.getTotalMHCII() <= 200000 ) 
						{ setS1P1(a);}
					if (lymph_node3DContext.getTotalMHCII()> 200000 ) 
						{ setS1P1(S1P1all_inflam);}
				 }
			
			 
			 else {
				 //no action on cells that have just entered
			 }
		}
				
		@Override
		public void setS1P1(double value)
		{
			  this.S1P1 = value;
		}	
	
		 public double getS1P1()
		  {
			  return S1P1;
		  }
		
	
}
