package lN_simple3;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.engine.watcher.Watch;
import repast.simphony.engine.watcher.WatcherTriggerSchedule;
import repast.simphony.parameter.Parameters;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
//import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import repast.simphony.valueLayer.GridValueLayer;

import java.util.ArrayList;
import java.util.List;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.engine.watcher.Watch;
import repast.simphony.engine.watcher.WatcherTriggerSchedule;
import repast.simphony.parameter.Parameters;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import repast.simphony.valueLayer.GridValueLayer;


public class DC {
  

  //  private ContinuousSpace<Object> space;
    private Grid<Object> grid;
    private int retainTime;
    private double MHCI;
    private double MHCII;
    private int boundCount;
    public int TcellsContacted;
    public int cogTcellsContacted;
    private double initialMHCI;
    private double initialMHCII;
    public int age;
    public boolean licenced; 

public DC (
		Grid<Object> grid,
		int retainTime,
		double MHCI,
		double MHCII,
		int boundCount,
		int TcellsContacted,
		int cogTcellsContacted,
		int age,
		boolean licenced
		){

    this.grid = grid;
    this.retainTime = retainTime; 
    this.MHCI = MHCI;
    this.MHCII=MHCII;
    this.boundCount = boundCount;
    this.TcellsContacted = TcellsContacted;
    this.cogTcellsContacted = cogTcellsContacted;
    this.initialMHCI = MHCI;
    this.initialMHCII = MHCII;
    this.age = age;
    this.licenced = false;
}

@ScheduledMethod(start=1,interval=1,priority = 3)
public void toRetain()
{
	
	// firstly check if the bound count is already reached. 
	int boundcount = getBoundCount();
	if (boundcount < Constants.DC_MAX_BOUND)
	
	{
		//System.out.println("retainment method");
		  Context<Object> context = (Context)ContextUtils.getContext(this);
		  GridPoint pt = grid.getLocation(this);
		  List<Tcell>Tcells = new ArrayList<Tcell>();
		  List<GridCell<Tcell>> gridCells = getInsideGrids(pt);
		  int i = 0;
		  for (i = 0; i < gridCells.size();i++){
			  for (Object Tcell : gridCells.get(i).items())
			  	{	if (Tcell instanceof Tcell)
			  			{Tcells.add((Tcell)Tcell);} //not sure this casting will work tbh
			  	}
		  	}
		  List<Tcell>FilteredFreeTcells = new ArrayList<Tcell>();
		  //System.out.println("nearby T cell list size = "+gridCells.size() );
		  //don't need to update timesincelastbund 
		  //don't need to alter bound count early at all
		  //don't want to update the stimulation here either. 

		  for (Tcell FreeTee: Tcells)
		  {
    		int retention = FreeTee.getRetention();
    		int timeSinceLastBound = FreeTee.getTimeSinceLastBound();
    		if (retention ==0 && timeSinceLastBound > 15 )
    		{
    			FilteredFreeTcells.add((Tcell)FreeTee); 
    		}
		  }
		  //now pick one of these eligible cells to retain 
		  // access the end node and set as retained. //don't actually need to update timesincelast bind at this point. 
		  //do update boundCount. 
		  //do access retention value and update
    	
		  if (FilteredFreeTcells.size() > 0) 
		  {
			int index = RandomHelper.nextIntFromTo(0,FilteredFreeTcells.size()-1);
			int Newretention  = RandomHelper.createNormal(Constants.noncog_interaction,2).nextInt(); //normal distribution, instead of weinbull	
			int Newretention2 = RandomHelper.nextIntFromTo(Constants.short_bind,Constants.short_bind+15);//)(30, 45); //10-15mins
			int Newretention3 = RandomHelper.nextIntFromTo(Constants.long_bind,Constants.long_bind+70);//(150, 210);//50-70mins
			
			if (FilteredFreeTcells.get(index) instanceof UncognateCell)
			{
				FilteredFreeTcells.get(index).setRetention(Newretention);
				Network<Object>net=(Network<Object>)context.getProjection("DC interaction network");
				net.addEdge(this,FilteredFreeTcells.get(index));
			//	Tcell tcell = FilteredFreeTcells.get(index);
				
				
			}
			
			
			if (FilteredFreeTcells.get(index) instanceof CognateCell )
			{
				CognateCell temp = (CognateCell) FilteredFreeTcells.get(index); 
				//if a memory cell
				if(temp.getM()==true){
					FilteredFreeTcells.get(index).setRetention(Newretention);
					Network<Object>net=(Network<Object>)context.getProjection("DC interaction network");
					net.addEdge(this,FilteredFreeTcells.get(index));}
				
				//if itmesincefirstbind < 8hours or proliferatign cell
				else if (temp.getTimeSinceFirstBind()  < Constants.time_bind_change || temp.getProfCount()> 0)
					{
					FilteredFreeTcells.get(index).setRetention(Newretention2);
					Network<Object>net=(Network<Object>)context.getProjection("DC interaction network");
					net.addEdge(this,FilteredFreeTcells.get(index));}
					//projection runs from DC to the T cell. source>target
				else 
					{
					temp.setRetention(Newretention3);
					Network<Object>net=(Network<Object>)context.getProjection("DC interaction network");
					net.addEdge(this,FilteredFreeTcells.get(index));
					}
			}	
		  	}// close if filtered free t cell > 0
		  //update bound count
				int newBC =  getBoundCount() + 1 ;
				setBoundCount(newBC);
	}// close the check max bound
	else
	{};// do not try to bind anything
				
}//this closes the boundcount section
	  
	  
	  
    
 


//this method makes a list of grid cells and the agents that they contain in the extent. 
public List<GridCell<Tcell>> getInsideGrids(GridPoint pt)
{ Context<Object> context = (Context)ContextUtils.getContext(this);
GridValueLayer geometryLayer = (GridValueLayer)context.getValueLayer("Geometry");
GridCellNgh<Tcell>nghCreator = new GridCellNgh<Tcell>(grid,pt,
        Tcell.class,2,2,2); // this retains in the extend of 2 
List<GridCell<Tcell>>gridCells = nghCreator.getNeighborhood(true);

//need to add a line of code here to filter out the points that are actually not inside the node
//List<GridCell<Cell>>gridCells = new  ArrayList<GridCell<Cell>>();

return gridCells;

}

@ScheduledMethod(start = 1, interval = 1, priority = 2)
public void checkAge() throws IOException
{
	double Number = RandomHelper.createNormal(Constants.DClifespan,500).nextDouble();//+/- 3hours //2.5 days = 32hours 10800
	if (getAge() > Number )
	{
		//get all projections
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
			if (Node instanceof Tcell )
			{
				((Tcell)Node).setRetention(0);
				((Tcell)Node).setTimeSinceLastBound(0);
				int DCcont = ((Tcell)Node).getDCContacted();
				if(DCcont > 0){((Tcell)Node).setDCContacted((DCcont+1));} //(was so you only track newly entered cells
				net.removeEdge(net.getEdge(this,Node));
			}	
		}			
		//remove self
		lymph_node3DContext.removeDC(this);
	}
}




@ScheduledMethod(start=1,interval=1,priority = 4)
public void updateProjections()
{	
	//1. get projections
	Context<Object> context = (Context)ContextUtils.getContext(this);
	Network<Object>net=(Network<Object>)context.getProjection("DC interaction network");
	//2.get Nodes
	List<Object> listNodes = new ArrayList<Object>();
	//net.getEdges(this);		
		for (Object node: net.getAdjacent(this))
			{
				listNodes.add(node);
			}
	//3. For each node  > alter the stim, > check retain state
		for (Object Node : listNodes)
			{  //check it works
				if (Node instanceof CognateCell )
					{	//stem.out.println("Node is being a cognate cell");
							alterStim((CognateCell) Node);
					}	
					
				if (Node instanceof Tcell )
						{
						//4. If retain has reached 1 then set last bind to 0 , set retain to 0 ,
						//5. remove the asscociated projection and set the DC bound count tp -1 
							int ret = ((Tcell) Node).getRetention();
								if (ret ==1)
									// release node
									{   ((Tcell)Node).setRetention(0);
										((Tcell)Node).setTimeSinceLastBound(0);	
										//checklicencing
											if (Node instanceof CognateCell)
												{
		//**comment out to shorten code	    	//update DC count of cognate cells interacted with
												int temp2 = getcogTcellsContacted()+1;
												setcogTcellsContacted(temp2);
												
													if (((CognateCell)Node).getActivation()==true)
														{setLicenced(true);}
												}
									//count both how many T cells a DC has contacted overall
									//all cells are setContact zero bar new cells so data is only taken from newly entered tcekks
									//if you do then remove the if clause
									int DCcont = ((Tcell)Node).getDCContacted();
									if(DCcont > 0){((Tcell)Node).setDCContacted(DCcont+1);}
									
		//**comment out to shorten code			//update DC with T cells contacted 
									int temp = getTcellsContacted() + 1;
									setTcellsContacted(temp);
		
																	
		
									net.removeEdge(net.getEdge(this,Node));
									int current = getBoundCount() - 1;
									setBoundCount(current);
		
									}
					}
			}
}

public void alterStim(CognateCell RetainedCell)
{	
    double oldStim = RetainedCell.getStimulation();
    if (RetainedCell.getCD4()==true){
    double MHClevel = getMHCII();
    double newStim = oldStim + (Constants.Ks * MHClevel);
    RetainedCell.setStimulation(newStim);
 //System.out.println("Tcell CD4 has" + newStim +" of stimulation");
    }
    else if (RetainedCell.getCD8()==true)
    	 {double MHClevel = getMHCI();
        double newStim = oldStim + (Constants.Ks * MHClevel);
        RetainedCell.setStimulation(newStim);
   // System.out.println("Tcell CD8 has" + newStim +" of stimulation");
    }
}


@ScheduledMethod(start=1,interval=1,priority = 10) // includes ageing
public void MHCdecay(){
//double oldMHC = getMHC();//oldMHC*0.9999;
//double t = RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
// this should really be timesince entered or age?
double t = getAge()/3/60; //convert to hours

//double lambdaM_1 = Constants.halflife1;
double tt= t/Constants.halflife1; //convert hours to time steps
double newMHC = getInitialMHCI() * Math.pow(0.5,tt);
setMHCI(newMHC);   

double tt2= t/Constants.halflife2; // convert hours to timesteps
//double lambdaM_2 = Constants.halflife2;
double newMHC2 = getInitialMHCII() * Math.pow(0.5,tt2);
setMHCII(newMHC2);  

int oldAge = getAge();
setAge(oldAge + 1);
}

public void setMHCI(double somevalue){
    this.MHCI = somevalue;  
}
public void setMHCII(double somevalue){
    this.MHCII = somevalue;  
}

public double getMHCI()
{
    return MHCI;
}
public double getMHCII()
{
    return MHCII;
}

public void setBoundCount(int value)
{
	this.boundCount = value;
}
public int getBoundCount()
{
	return boundCount;
}
public int getTcellsContacted()
{
	return TcellsContacted;
}

public void setTcellsContacted(int value)
{
	this.TcellsContacted = value;
}
public int getcogTcellsContacted()
{
	return cogTcellsContacted;
}

public void setcogTcellsContacted(int value)
{
	this.cogTcellsContacted = value;
}




public double getInitialMHCI()
{
	return initialMHCI;
}

public double getInitialMHCII()
{
	return initialMHCII;
}



public int getAge()
{
	return age;
}

public void setAge(int value)
{
	this.age = value;
}

private void setLicenced(boolean b) {
	this.licenced = b;
}

public boolean getLicenced()
{
	return licenced;
}
}
      