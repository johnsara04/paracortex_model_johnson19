package lN_simple5;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.random.RandomHelper;
//import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.graph.Network;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.SimUtilities;



public class CognateCell extends Tcell {
	
	//extra fields
	
	public int timeSinceFirstBound;
	public boolean Activated;
	public boolean Effector;
	public int proliferationCount; //when initialising, would grab this from the parent
	public double stimulation;
	public double S1P1;
	public int istring;
	public boolean trackingvalue;
	public int timeSinceFirstAct;
	public boolean CD4;
	public boolean CD8;
	public boolean M; 
	public int TimeSinceDif;
	//constructor
	public CognateCell(
			//ContinuousSpace<Object>space, // no longer use continuous space
			Grid<Object>grid,
		   int retainTime,
		   int age,
           int timeSinceLastBind,
           int timeSinceEntered,
           int DCContacted,
           int timeFirst,
           double thisStim,
           boolean activated,
           boolean effector,
           int profCount,
           double S1p1,
           int iString,
           boolean Trackingvalue,
           int timeAct,
           boolean cd4,
           boolean cd8,
           boolean m,
           int timeSinceDif
			)
	{		
		super (//space,
				grid,retainTime,age,timeSinceLastBind,timeSinceEntered,DCContacted,S1p1,iString,Trackingvalue);
		Activated = activated ;
		Effector=effector;
		proliferationCount = profCount ;
		S1P1 = S1p1;
		istring = iString;
		trackingvalue = Trackingvalue;
		timeSinceFirstBound = timeFirst;
		stimulation = thisStim;
		timeSinceFirstAct=timeAct;
		CD4 = cd4;
		CD8 = cd8;
		M = m;
		TimeSinceDif =timeSinceDif;
	
	}
	
@ScheduledMethod(start = 1, interval = 1 , priority = 7)
	public void updateCell()
	{	
	//update history
    if (timeSinceFirstBound > 0)
    		{ timeSinceFirstBound++;}  
    if (timeSinceFirstAct > 0 )
    		{timeSinceFirstAct++;}
    if (getTimeSinceDif() > 0)
    		{TimeSinceDif++;}
    //resetting time since last differentiated after 8hours 5 mins
    if (getTimeSinceDif() == 1455)
    		{setTimeSinceDif(1);} 
	//T cell has accumulated stimulation, let it decay down to 1 
    if (stimulation > 1)
    	{ updateStimulation();}  
    //if the T cell is naive , see if it can be activated ( bound or unbound) 
    //but only if it has some stimulation. (if it has ever interacted with a DC this will be >0)
    if (stimulation>0)
    {if (getActivation() == false && getEffector()==false && getM()==false )
    	{checkActivation();}}
    //if not bound 
   if (getRetention() ==0){
	   //checkProliferation + //reproduce   
	   if (getActivation()== true || getEffector()==true)
	   		{seeIfreproduceActivatedOrEffector();}
	   					  	}
   // See if T cell can differentiate (early effector) ( don't have to check if it is an effector already?)
   if (getProfCount() > 4 && getProfCount() < 8  && getM()==false)
   		{seeIfEffector();}
    
   // see if T cell can differentiate (late effector)// memory cells stay as memory cells
   if (getProfCount()>= 8  && getM() == false && getEffector()==true)
   		{seeIfMemoryorEff(); }
   }

//separate early and effector queries allow differences to be applied more easily

    	
//links between cells ( projections) are stored in 'infection network'
	public void removeLinks(){
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
			}			    }
	
	 @Override
	 public void checkAge()
	{ //if naive
	 if (Activated == false && Effector == false && getM() ==false)
		{
		 int n =  RandomHelper.createNormal(Constants.lifespanT,100).nextInt();
			if (getAge()> n){			
				removeLinks();
				lymph_node3DContext.removeCellAgeing(this);
			}
		}
	 
	 //if activated
	 if (Activated == true)
	 {	 int n = RandomHelper.createNormal(Constants.lifespan_ActT, 20).nextInt(); //(41 hours)
		 if (getAge()> n){
			 removeLinks();	
			 lymph_node3DContext.removeCellAgeing(this);
		 }	 
	 }//if effector
	 if (Effector == true)
	 {
		 //if antigenc stimuli disappeared, effectors apoptose rapidly // this removed as short anyway
		 
		 int n = RandomHelper.createNormal(Constants.lifespan_EffT,200).nextInt(); //(3days hours)//was 41 //15120,
		 //int n2 = RandomHelper.createNormal(Constants.lifespan_EffT, 180).nextInt(); //(12hours)//could alter 2160
		// if (lymph_node3DContext.getTotalMHCII()>100)
		 //	{
			 	if (getAge()>n)
			 		{ removeLinks();		
			 		lymph_node3DContext.removeCellAgeing(this);
			 		}
		 }
	//	if (lymph_node3DContext.getTotalMHCII()<=100)
	//		{  if (getAge()>n2)
	//			{ removeLinks();			
	//			lymph_node3DContext.removeCellAgeing(this);
	//			}
	//		}
	// }
	 //no lifespan for memory as beyond scope of the model 
	}
	 
	 
	 @Override
	  public void seeIfExit() throws IOException
	    {
		 //activated cells upregulate CD69 and downregulate S1P1r so reducing exit probability 
	    //Bound cells do not exit so no need to remove links as it's already free
		 Parameters params = RunEnvironment.getInstance().getParameters();
	    	double value = RandomHelper.nextDoubleFromTo(0, 100000)/100000;
	    	double exitProb = (Double)params.getValue("Pe")*(getS1P1()); 
	    	if (value < exitProb)
	    		{
	    			lymph_node3DContext.removeCellExit(this);
	    		}
	    	else
	    		checkAge();
	    }
	 
	 @Override
	  public void seeIfExit2() throws IOException
	    {
		 //activated cells upregulate CD69 and downregulate S1P1r so reducing exit probability 
	    //Bound cells do not exit so no need to remove links as it's already free
		 Parameters params = RunEnvironment.getInstance().getParameters();
	    	double value = RandomHelper.nextDoubleFromTo(0, 100000)/100000;
	    	double exitProb = (Double)params.getValue("Pe2")*(getS1P1()); 
	    	if (value < exitProb)
	    		{
	    			lymph_node3DContext.removeCellExit(this);
	    		}
	    	else
	    		checkAge();
	    }
	
    public void updateStimulation(){
    	Parameters params = RunEnvironment.getInstance().getParameters();
	    double stim = getStimulation();
	    stim = stim*(Double)params.getValue("decay");
	        setStimulation(stim);
									}
	
	public void checkActivation()
	{
	    Parameters params = RunEnvironment.getInstance().getParameters();
	    double S1P1act = (double)params.getValue("S1P1act");
		double x = RandomHelper.nextDoubleFromTo(0, 1000) / 1000;	
		//cd4
		int c1 = Constants.ACTIVATION_MEAN_CD4; //actully this is 
		double d1 = Constants.ACTIVATION_CURVE_CD4;
		
		//cd8
		int c2 = Constants.ACTIVATION_MEAN_CD8;
		double d2 = Constants.ACTIVATION_CURVE_CD8;
		//if a CD4 > always the same, use activation binding curve
		if (getCD4()==true){
		double ActProbCD4 = 1/ (1+ Math.exp(-((getStimulation() - c1)/d1)));
		if ( x < ActProbCD4 )
		{
			setActivation(true);
			// variation used to test different methods, 4 options defined in param file. 
			setS1P1(S1P1act);
			setTimeSinceFirstAct(1);
		}}
		
		//if a CD8> gotta check the DC to see if it is licenced, as this means it's more likely to activate a cell.
		if (getCD8()==true)
		{
		Context<Object> context = (Context)ContextUtils.getContext(this);
		Network<Object>net=(Network<Object>)context.getProjection("DC interaction network");
		for (Object node: net.getAdjacent(this)) //should only ever have one DC
			{
				if (node instanceof DC)	
					{
						if (((DC)node).getLicenced() ==true)
			
					{
						double ActProbCD4 = 1/ (1+ Math.exp(-((getStimulation() - c1)/d1))); //this value will be the Activation Binding Curve = 1/(1+exp{(-getStimulation() - c1)/d1))
						if ( x < ActProbCD4 )
							{
								setActivation(true);
								setS1P1(S1P1act);
								//System.out.println("CD8 activated with licenced DC");
								setTimeSinceFirstAct(1);
							}
					}
					}
			//else can still activate even if DC is not licensed or bound			
			else 
				{
					double ActProbCD8 = 1/ (1+ Math.exp(-((-getStimulation() - c2)/d2))); //this value will be the Activation Binding Curve = 1/(1+exp{(-getStimulation() - c1)/d1))
					if ( x < ActProbCD8 )
					{
						setActivation(true);
						setS1P1(S1P1act);
						setTimeSinceFirstAct(1);
					}	
				}
				
			}

		} //end of cd8 stuff
}
	
		// maybe pass in stimualtion and gridpoint location: why?
	
	public void seeIfreproduceActivatedOrEffector() 
    {            
		if (getCD4()==true){
			if (getProfCount() < Constants.MaxProfCD4 ){		//10 max
				int	somevalue = RandomHelper.createNormal(Constants.proftimeCD4, 90).nextInt();  
					if (getTimeSinceFirstAct() > somevalue)//(1980, 180))  //11hours,1hour
						{ 
							//if (getStimulation()>0.00){
								seeifreproduceCD4();
								//}
						}}}
		
		else if (getCD8()==true){
			if (getProfCount() < Constants.MaxProfCD8 ){		//16 profs MaxProfCD8
				
				int	somevalue = RandomHelper.createNormal(Constants.proftimeCD8, 90).nextInt();  
				if (getTimeSinceFirstAct() > somevalue)//(1260, 180)) //7hours
		{
				//System.out.println("Seeing if reproducing CD8");
				//if (getStimulation()>0.01){
					seeifreproduceCD8();
					//}
				}}}
    }
	
	public void seeifreproduceCD4()
	{	
		GridPoint thispoint = grid.getLocation(this);
        	int profCount = getProfCount() + 1;
            int parentAge = getAge();
            double parentS1P1 = getS1P1();
            boolean parentCD4 = getCD4();
            boolean parentCD8 = getCD8();
            boolean M = getM(); // this will be altered in the see if effector stage
            int daughterAge = 1; //parentAge / 2 ;
            double parentStimulation = getStimulation();
            double daughterStimulation = parentStimulation / 2 ;
            boolean activated = getActivation();
            boolean effector = getEffector(); //this means if activated/effector,produce daughter cells of the same type
            //instead of a new cell , just reset the age and stim on self?
            int TimeSinceDif = getTimeSinceDif();
            //update parent (to new daughter)
            setAge(daughterAge);
            setRetention(0);
            setTimeSinceLastBound(0);
            setStimulation(daughterStimulation);
            setProfCount(profCount);
            //setTimeSinceEntered(1); // leave the old counter on, so a better picture of how long these 
            //cells are actually in the node is give, then other cell is set at one. in this way you'll 
            //probably end up with some subsets. // to get rid of the subsets and have in one set, you 
            // need to set daughter same as parent
            setTimeSinceFirstAct(1);
          
           //could add regulation of S1P1 per profliferation here if you wanted to 
            
            //timesincefirst bound stays the same as it's overuled by prolif anyway
                        
            Context context = ContextUtils.getContext(this);
            
            //update other daughter
            int retainTime = 0;           
        	double S1p1 = parentS1P1;
        	istring = 0;
        	trackingvalue = false;
        	int timeAct = 1;//time since first act, used to counter the time since last proliferated
            int timeSinceLastBind = 0;
            int timeFirst = getTimeSinceFirstBind(); //first bound though maybe change this //doesn't really matter as timeFirst is now overule as prof>0
            int timeSinceEntered = 1; //so that is picked up as a counter
            int DCContacted = 0;
            boolean CD4 = parentCD4;
            boolean CD8 = parentCD8;
            
            
            CognateCell cell = new CognateCell(
            		//space,
            		grid, retainTime,age,timeSinceLastBind,
            		timeSinceEntered,
            		DCContacted,
            		timeFirst,
            		daughterStimulation,
            		activated,effector,profCount,S1p1,istring,trackingvalue,timeAct,
            		CD4,
            		CD8,
            		M,
            		TimeSinceDif);
            context.add(cell);
          //  System.out.println("Daugter  cell added CD4 activation time = " + getTimeSinceFirstAct());
            
            //method to get neighbors and filter the result and return a list 
            List<GridCell<Tcell>>inside = getInsideGrids(thispoint);
            //tbh this gives an array of grid cells not gridpoint> useful if you want to check the contents of the cells i suppose
            // could have a different method that just returns grid points, not their contents. 
            //this would be the manual + 1 -1 method, for now, just keep the cell method
            SimUtilities.shuffle(inside, RandomHelper.getUniform());
            
            if (inside.size() == 0)
            {
            	//System.out.println("nowhere to put daughter cells");
            	}
            else
            {
            GridPoint insidepoint = inside.get(0).getPoint();
            //shuffle list 
           // space.moveTo(cell,insidepoint.getX(), insidepoint.getY(),insidepoint.getZ());//space pt
            grid.moveTo(cell, insidepoint.getX(), insidepoint.getY(),insidepoint.getZ());
            
           int  count = lymph_node3DContext.getTCellCount();
           lymph_node3DContext.setTCellCount (count + 1);
}}     
	public void seeifreproduceCD8()
	{	
		GridPoint thispoint = grid.getLocation(this);
            int profCount = getProfCount() + 1;
            int parentAge = getAge();
            double parentS1P1 = getS1P1();
            boolean parentCD4 = getCD4();
            boolean parentCD8 = getCD8();
            boolean M = getM(); // this will be altered in the see if effector stage
            int daughterAge = 1; //parentAge / 2 ;
           
            double parentStimulation = getStimulation();
            double daughterStimulation = parentStimulation / 2 ;
            boolean activated = getActivation();
            boolean effector = getEffector(); //this means if activated/effector,produce daughter cells of the same type
            //instead of a new cell , just reset the age and stim on self?
            int TimeSinceDif = getTimeSinceDif();
            //update parent (to new daughter)
            setAge(daughterAge);
            setRetention(0);
            setTimeSinceLastBound(0);
            setStimulation(daughterStimulation);
            setProfCount(profCount);
           // setTimeSinceEntered(1); see comments as above. 
            setTimeSinceFirstAct(1);
            //timesincefirst bound stays the same as it's overuled by prolif anyway
            
            Context context = ContextUtils.getContext(this);
             
            //update other daughter
            int retainTime = 0;           
        	double S1p1 = parentS1P1;
        	
        	istring = 0;
        	trackingvalue = false;
        	int timeAct = 1;//time since first act, used to counter the time since last proliferated
            int timeSinceLastBind = 0;
            int timeFirst = getTimeSinceFirstBind(); //first bound though maybe change this //doesn't really matter as timeFirst is now overule as prof>0
            int timeSinceEntered = 1; //so that is picked up as a counter
            int DCContacted = 0;
            boolean CD4 = parentCD4;
            boolean CD8 = parentCD8;
            
            CognateCell cell = new CognateCell(
            		//space, 
            		grid, retainTime,age,timeSinceLastBind,
            		timeSinceEntered,
            		DCContacted,
            		timeFirst,
            		daughterStimulation,
            		activated,effector,profCount,S1p1,istring, trackingvalue , timeAct,
            		CD4,
            		CD8,
            		M,
            		TimeSinceDif);
            context.add(cell);
          //  System.out.println("Daugter  cell added CD8");
            
            //method to get neighbors and filter the result and return a list 
            List<GridCell<Tcell>>inside = getInsideGrids(thispoint);
            //tbh this gives an array of grid cells not gridpoint> useful if you want to check the contents of the cells i suppose
            // could have a different method that just returns grid points, not their contents. 
            //this would be the manual + 1 -1 method, for now, just keep the cell method
            SimUtilities.shuffle(inside, RandomHelper.getUniform());
            
            if (inside.size() == 0)
            {
            	//System.out.println("nowhere to put daughter cells");
            	}
            else
            {
            GridPoint insidepoint = inside.get(0).getPoint();
            //shuffle list 
          //  space.moveTo(cell,insidepoint.getX(), insidepoint.getY(),insidepoint.getZ());//space pt
            grid.moveTo(cell, insidepoint.getX(), insidepoint.getY(),insidepoint.getZ());
            
           int  count = lymph_node3DContext.getTCellCount();
           lymph_node3DContext.setTCellCount (count + 1);
}} 
	
	
public void seeIfEffector()
	{	
	//add the time since dif to between 8 hours and 8hours 5 
	//if time since dif is zero, meaning it has never been differentiated before or getTimeSinceDif > 1440 (8hours)
	//and always set back to 1 to start counter
	if (getTimeSinceDif()==0 || getTimeSinceDif() >1440   ){//shouldn't need to specify as reset in update&& getTimeSinceDif() <1455
		
		
	
			double x = RandomHelper.nextDoubleFromTo(0, 1000)/1000;	
			int e1 = Constants.EFFECTOR_MEAN_CD4;
			double f1 = Constants.EFFECTOR_CURVE_CD4;
			int e2 = Constants.EFFECTOR_MEAN_CD8;
			double f2 = Constants.EFFECTOR_CURVE_CD8;
			
			double effProbCD4 = 1/ (1+ Math.exp(-((-getStimulation() - e1)/f1))); //this value will be the Effector differentiation Curve = 1/(1+exp{(-getStimulation() - e1)/f1))
			double effProbCD8 = 1/ (1+ Math.exp(-((-getStimulation() - e2)/f2)));

			if (getCD4()==true)
			{if ( x < effProbCD4 )
			{	
				if (RandomHelper.nextIntFromTo(0, 10000)< (Constants.early_dif_ratio*10000)) 
				{//make memory cell} 
					setEffector(false);
					//setTimeSinceFirstAct(1); // as will only be updated if > 0/  don't need to touch this
					setS1P1(Constants.S1P1mem);
					setTimeSinceFirstAct(1);	// this affects the interaction behaviour				
					setActivation(false); //no need to alter, actually there is otherwise counted in ifs
					setTimeSinceDif(1); // instead do this
					setM(true);
					//tActivation(false); //removes from the reproduction cycle 
					//System.out.println("Memory Cell made");
				}
				else{
				setEffector(true);
				//setTimeSinceFirstAct(1); // as will only be updated if > 0/ 
				setTimeSinceDif(1); // instead do this
				setS1P1(Constants.S1P1eff_early);				
				setActivation(false); // i think this is ok. 
			}}}
			else
			{
				if ( x < effProbCD8 )
				{
					if (RandomHelper.nextIntFromTo(0, 10000)< (Constants.early_dif_ratio*10000)) 
					{//make memory cell} 
						setEffector(false);
						//setTimeSinceFirstAct(1); // as will only be updated if > 0/ 
						setTimeSinceFirstAct(1); //testing
						setTimeSinceDif(1); // instead do this
						setS1P1(Constants.S1P1mem);
						setActivation(false);
						setM(true);
					}
					else{
					setEffector(true);
					//setTimeSinceFirstAct(1); // as will only be updated if > 0/ 
					setTimeSinceDif(1); // instead do this
					setS1P1(Constants.S1P1eff_early);					
					setActivation(false); // i think this is ok. 
					}
			}
			}
		}
	}
public void seeIfMemoryorEff()
{
	
	//this will be to take cells with prolif > 6 and see if they differentiate into memory cells or continue as effectors.
	// is a requisite to be an effector already
	//so activation = 0, effector = 0, CM = 1 
	
	if (getTimeSinceDif() >1440  ){ // no need for upper limit as it's always set back to 1 in the update section
		//could maybe alter this into a joint method
		double x = RandomHelper.nextDoubleFromTo(0, 1000)/1000;	
		int e1 = Constants.EFFECTOR_MEAN_CD4;
		double f1 = Constants.EFFECTOR_CURVE_CD4;
		int e2 = Constants.EFFECTOR_MEAN_CD8;
		double f2 = Constants.EFFECTOR_CURVE_CD8;
		
		double effProbCD4 = 1/ (1+ Math.exp((-getStimulation() - e1)/f1)); //this value will be the Effector differentiation Curve = 1/(1+exp{(-getStimulation() - e1)/f1))
		double effProbCD8 = 1/ (1+ Math.exp((-getStimulation() - e2)/f2));
		
		if (getCD4()==true)
		{
		if (x< effProbCD4)
		{
			if (RandomHelper.nextIntFromTo(0, 10000)< (Constants.late_dif_ratio *10000) ) 
			{//make memory cell} 
				setEffector(false);
				//setTimeSinceFirstAct(1); // as will only be updated if > 0/ 
				setTimeSinceDif(1); // instead do this
				setS1P1(Constants.S1P1mem);
				setActivation(false);
				setM(true);
				//System.out.println("Memory Cell made");
			}
			else{
			setEffector(true);
			//setTimeSinceFirstAct(1); // as will only be updated if > 0/ 
			setTimeSinceDif(1); // instead do this
			setS1P1(Constants.S1P1eff_late);
			setActivation(false); // i think this is ok. 
		}}}
		else{
			if ( x < effProbCD8 )
			{
				if (RandomHelper.nextIntFromTo(0, 10000)< (Constants.late_dif_ratio*10000)) 
				{//make memory cell} 
					setEffector(false);
					//setTimeSinceFirstAct(1); // as will only be updated if > 0/ 
					setTimeSinceDif(1); // instead do this
					setS1P1(Constants.S1P1mem);
					setActivation(false);
					setM(true);
					//System.out.println("Memory Cell made");
				}
				else{
				setEffector(true);
				//setTimeSinceFirstAct(1); // as will only be updated if > 0/ 
				setTimeSinceDif(1); // instead do this
				setS1P1(Constants.S1P1eff_late);
				setActivation(false); // i think this is ok. 
			}}}		}
}
	
@Override
public void updateInitalS1P1 ()
{	if (getActivation()==false && getEffector()==false && getM()==false)
	{setS1P1(1);};
}


@Override
public void checkInflammation()  //acts on cognate cells that are not activated at start and new
//ones that enter at the end of the stim. 
{
	if(getActivation() ==false && getEffector()==false && getM()==false)
	{
	
	 if (timeSinceEntered == 0 || timeSinceEntered > Constants.responsetimeInflam) //4hours
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
	 //because we want it to go back to normal when this isn't true
	 }
	else {
		//don't act on activated or effector
	}	
}
		
	public int getProfCount()
	{
		return proliferationCount;
	}
	public void setProfCount(int value)
	{
		this.proliferationCount = value;
	}
	
	 public void setTimeSinceFirstBind(int value)
	 {this.timeSinceFirstBound = value;}
	    
	 public int getTimeSinceFirstBind()
	  { 	return timeSinceFirstBound;
	    }
	 
public void setStimulation(double newstimulation)
	    {
	    this.stimulation = newstimulation;
	    
	    }
	    
 public double getStimulation()
	    {
	        return stimulation;
	    }
 public boolean getActivation(){
	 return Activated;
	 }
 private void setActivation(boolean value)
 {
	 this.Activated = value;
	 //this.setTimeSinceFirstAct(1);
 }
 
 private void setEffector(boolean value)
 {
	 this.Effector = value; //but need to set Activated false?  
 }
 
 public boolean getEffector()
 {
	 return Effector;
 }
 
 public void setTimeSinceFirstAct(int value)
 {
	 this.timeSinceFirstAct = value;
 }
 
 public int getTimeSinceFirstAct()
 {
	 return timeSinceFirstAct;
 }

 public boolean getCD4()
 {
	 return CD4; 
 }
 public void setCD4(boolean value)
 {
	 this.CD4 = value;
 }
 
 public boolean getCD8()
 {
	 return CD8;
 }
 public void setCD8(boolean value)
 {
	 this.CD8 = value;
 }
 public boolean getM()
 {
	 return M;
 }
 public void setM(boolean value)
 {
	 this.M = value;
 }
public int getTimeSinceDif()
{
	return TimeSinceDif;
}
public void setTimeSinceDif(int value)
{
this.TimeSinceDif= value;
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
