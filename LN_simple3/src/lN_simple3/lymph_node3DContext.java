 package lN_simple3;

	import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

	import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.Schedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.random.RandomHelper;
	//import repast.simphony.space.continuous.ContinuousSpace;
	//import repast.simphony.space.continuous.NdPoint;
	import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.SimUtilities;
import repast.simphony.valueLayer.GridValueLayer;
import repast.simphony.valueLayer.ValueLayer;
import static java.nio.file.StandardCopyOption.*;

public class lymph_node3DContext  extends DefaultContext<Object>  {
		
		int DCnumber = 0;
		int gridNumber = 0;
		public int DCsToAdd=0; // will be reset in the the DCadding method
		//original outer and inner that stay constant
		double outer_radius =  Constants.OUTER_RADIUS;
		double inner_radius = Constants.INNER_RADIUS;
		//geometry parameters
		double exit_percent = Constants.EXIT_PERCENTAGE;
		double entry_percent = Constants.ENTRY_PERCENTAGE; 
		int gridWidth = Constants.GRID_WIDTH;
		int gridHeight = Constants.GRID_SIZE;
		int gridz = Constants.GRID_SIZE;
		int centreP = Constants.CENTRE_POINT;
		int centreX = 0;
		int half_world = gridHeight/2; 
		//these inner and outers alter and overwritten on timestep 1 but needed for initiation  but why??
		private double new_inner = inner_radius;
		public double outerRadius = outer_radius;
		public double LastAdd=1;
		//this is used when equal exit volume : inner Hev rate of volume change is wanted:
		public double exitRadiusEq =Constants.OUTER_RADIUS*Constants.EXIT_PERCENTAGE;
		public double hevRadiusEq =Constants.OUTER_RADIUS*Constants.ENTRY_PERCENTAGE;
			
		//calculation parameters
		private static double initialBV= 0 ;
		private static double initialPV=0; // the paracortex area that isn't EV
		private static double initialEV=0; // currently isn't used, but calculated at initialiseBV
		private static double initialV = 0;
		private static double sigmoid_correction = 0;
		private double volume;
		private double Bv; //current Bv volume in micrometers cubed
		private double Influx;
		public double recruitment = 1;
		private static double totalMHCII = 0;;
		private static double totalMHCI= 0;
		private static int celltrackcounter=1;
		
			//counters 
		private static int Tcell_count =Constants.Initial_Tcellcount;
		private static int DCCellCount = 0; //not including initial DCs if any
			// counters for the T cells
		private static int count_of_Tcells_exited = 0;
		private static int count_of_Tcells_entered = 0;
		private static int active_exited= 0;
		private static int eff_exited = 0;
		private static int cog_exited = 0;
		private static int memory_exited = 0;
		private static int active_exited4= 0;
		private static int eff_exited4 = 0;
		private static int cog_exited4 = 0;
		private static int memory_exited4 = 0;
		private static int active_exited8= 0;
		private static int eff_exited8 = 0;
		private static int cog_exited8 = 0;
		private static int memory_exited8 = 0;

		//can comment out counters and relavent getters and setters if needed
		private static int cell_count_cd4;
		private static int cell_count_cd8;
		private static int cell_count_M;
		private static int cell_count_A;
		private static int cell_count_E;

		private static int cell_count_cd4_A =0;
		private static int cell_count_cd8_A= 0;
		private static int cell_count_cd4_E=0;
		private static int cell_count_cd8_E=0;
		private static int cell_count_cd4_M=0;
		private static int cell_count_cd8_M=0;

			//public lists
		public List<GridPoint>FreePoints= new ArrayList<GridPoint>();
			//tracking //not su
	//	public boolean TRACKING= false;
		public static ArrayList<Tcell>TrackingList = new ArrayList();
		
			//counters on initiation
		@ScheduledMethod(start = 1, interval = 180)// whenever you want to call it. 
		public void CellCounters(){
			Object obj2 = getRandomObject();
			setCellCountCD4(0);
			setCellCountCD8(0);
			setCellCountM(0);
			setCellCountA(0);
			setCellCountE(0);
		//add if line to be able to turn this off > since it iterates over every object in the context	
	
		if (Constants.CellCountON==1){;
		
			Context<Object>context =(Context)ContextUtils.getContext(obj2);
		for (Object obj:context){
			if (obj instanceof CognateCell)
			{
				if (((CognateCell)obj).getCD4() == true)
					{cell_count_cd4++;}
				if (((CognateCell)obj).getCD8() == true)
					{cell_count_cd8++;}
				if (((CognateCell)obj).getM() == true)
					{cell_count_M++;}
				if (((CognateCell)obj).getActivation() == true)
					{cell_count_A++;}
				if (((CognateCell)obj).getEffector() == true)
					{cell_count_E++;}}}
		}// end of boolean switch If
		}
		
		//@ScheduledMethod(start = 10000,priority =13) //copy method to copy download results from HPC at time step 40000
		//public void copystuff1()
		//{   copyResults.copy("dump1"); }
				
	//	@ScheduledMethod(start = 22000,priority =13) 
	//	public void copystuff2()
	//	{	copyResults.copy("dump2"); }
				
	//	@ScheduledMethod(start = 41000,priority =13) 
	//	public void copystuff3()
	//	{   copyResults.copy("dump3"); }
		
	// Method to end the simulation if the number of T cells gets too high (cause crash)			
	@ScheduledMethod(start = 50, interval = 30, priority =14) // after equilibrium reaches , a new start point
		public void endRun()
	{
			Object obj = getRandomObject();
			Context<Object>context =(Context)ContextUtils.getContext(obj);
			if (context.getObjects(Tcell.class).size() >= 800000) {
				System.out.println("Ending the simulation early : Tcells = 800000");
				copyResults.copy("endingEarly");
				RunEnvironment.getInstance().endRun();
				}
		}
	
		// DC placement initialised just before DCs begin arriving
	@ScheduledMethod(start = (Constants.TICK_DCS_ENTER-2)) 
		public void DCplacementList() 
		{	
			System.out.println("Initial Tcells in DC method"+ Constants.Initial_Tcellcount);
			setDCnumber((int)(Constants.Initial_Tcellcount*Constants.DCpercentage));//0.02162
			System.out.println("Number of DCs: "+ getDCnumber());
			double Inner_Radius = getInnerRadius();		
			
			Grid grid = (Grid)getProjection("grid");
			for (int r =0; r < gridWidth; r++)
				{
			    	for (int j = 0; j < gridHeight; j++)
			    		{
			    			for (int k = 0; k < gridHeight; k++)
			    				{
			    					GridPoint centre = new GridPoint(centreX,centreP,centreP);
			    					GridPoint otherPoint= new GridPoint(r,j,k);
			    					if (grid.getDistance(otherPoint,centre) < (Inner_Radius-1))//26
			    						{
			    							FreePoints.add(otherPoint);
			    						}
			    				}
			    		}}
		System.out.println("number of free grids DCs :"+ FreePoints.size());
		System.out.println("Constants id value or random number: "+Constants.randomnumber);
		System.out.println("Current directory = " + (System.getProperty("user.dir")));
		}
		
		//Initial volumes		
		@ScheduledMethod(start = 1,priority =13)
		public void initialiseValues() //halved as it is a hemisphere
		{
			initialV = (((Math.pow((Constants.OUTER_RADIUS*6),3.0)) * (Math.PI)*4/3)/2 );//*(Math.pow(10,-9)); // not currently used
			initialBV =(((Math.pow((Constants.OUTER_RADIUS*6*Constants.ENTRY_PERCENTAGE),3.0)) * (Math.PI)*4/3)/2 );//don't need in Meters*(Math.pow(10,-9));
			initialPV =(((Math.pow((Constants.OUTER_RADIUS*6*Constants.EXIT_PERCENTAGE),3.0)) * (Math.PI)*4/3)/2 );
			
			System.out.println("Initial T cell count= "+ Constants.Initial_Tcellcount);
			System.out.println("Initial Volume = " + initialV);
			System.out.println("Initial Radius in micrometers = " + Constants.OUTER_RADIUS*6);
			System.out.println("Initial Bv in micrometers" + initialBV);
			setHEVRadiusEq(Constants.OUTER_RADIUS*Constants.ENTRY_PERCENTAGE); // in grids not micrometers
			setExitRadiusEq(Constants.OUTER_RADIUS*Constants.EXIT_PERCENTAGE);
			System.out.println("Initial Bv radius in micrometers" + Constants.OUTER_RADIUS*6*Constants.ENTRY_PERCENTAGE);
			//this first part adds any DCs added in context to the counter as params could not be passed to the static reference.
			int ivalue = Constants.initialDC;
			int d = 0 ;
			while (d < ivalue)
			{
				DCCellCount++;
				d++;
			}
		System.out.println("Random value for transit = " + Constants.randomnumber)	;
		System.out.println("Number of T cells to track = " + Constants.numTcellsToTrack);
		System.out.println("Beta =" + Constants.Beta);
		System.out.println("noncog_interaction= "+ Constants.noncog_interaction);
		
		}
		//reinitialise volumeS values JUST before recruitment change
		@ScheduledMethod(start = Constants.TICK_BV_RECRUITMENT_CHANGE-1,priority =13) // one step before entry starts varying once equilibrium reached rather then time 0
			public void initialiseBVValues() //halved as it is a hemisphere
			{			
			initialV = (((Math.pow((getOuterRadius()*6),3.0)) * (Math.PI)*4/3)/2 );//*(Math.pow(10,-9)); // not currently used
			System.out.println("Inital Volume reinitialised = " + initialV);			
			initialBV =(((Math.pow((getOuterRadius()*6*Constants.ENTRY_PERCENTAGE),3.0)) * (Math.PI)*4/3)/2 );//don't need in Meters*(Math.pow(10,-9));
		//	double initialBV2 =(((Math.pow((hevRadiusEq*6),3.0)) * (Math.PI)*4/3)/2 );
			System.out.println("reinitialised BV = " + initialBV );
			initialPV =(((Math.pow((getOuterRadius()*6*Constants.EXIT_PERCENTAGE),3.0)) * (Math.PI)*4/3)/2 );
			System.out.println("reinitialised PV = " + initialPV );

			 double outvolume= (((Math.pow((getOuterRadius()*6),3.0)) * (Math.PI)*4/3)/2 );
			double involume = (((Math.pow((getOuterRadius()*6*Constants.EXIT_PERCENTAGE),3.0)) * (Math.PI)*4/3)/2 );
			initialEV= outvolume-involume;	// currently not used (too complicated)
			//important to update these for the rate change methods: i think need to update anyway
			setHEVRadiusEq(getOuterRadius()*Constants.ENTRY_PERCENTAGE); // in grids not micrometers
			setExitRadiusEq(getOuterRadius()*Constants.EXIT_PERCENTAGE);
		}
		
		//reinitialise blood volume values
		@ScheduledMethod(start = Constants.TICK_VOLUME_CHANGE+1,priority =13) // one step AFTER VOLUME CHANGE
			public void initialiseVValues() //halved as it is a hemisphere
			{			
			initialV = (((Math.pow((getOuterRadius()*6),3.0)) * (Math.PI)*4/3)/2 );//*(Math.pow(10,-9)); // not currently used
			System.out.println("Inital Volume reinitialised = " + initialV);
			initialBV =(((Math.pow((getOuterRadius()*6*Constants.ENTRY_PERCENTAGE),3.0)) * (Math.PI)*4/3)/2 );//don't need in Meters*(Math.pow(10,-9));
			System.out.println("reinitialised BV = " + initialBV );
			initialPV =(((Math.pow((getOuterRadius()*6*Constants.EXIT_PERCENTAGE),3.0)) * (Math.PI)*4/3)/2 );
			System.out.println("reinitialised PV = " + initialPV );
			//important to update these for the rate change methods
			setHEVRadiusEq(getOuterRadius()*Constants.ENTRY_PERCENTAGE); // in grids not micrometers
			setExitRadiusEq(getOuterRadius()*Constants.EXIT_PERCENTAGE);
		}
		
		


//	@ScheduledMethod(start = 6000, interval = 2, priority = 11)
//public void killDCs()
//{Object obj = getRandomObject();Context<Object>context =(Context)ContextUtils.getContext(obj);
//	List<DC>DCs = new ArrayList<DC>();
//	for (Object dcs : context.getObjects(DC.class)) 
//		{DCs.add((DC)dcs);}
//		if (DCs.size()>10)
//			{DC goner = DCs.get(1);
//			((DC)(goner)).setAge(80000);}
//			DCs.clear();}
	
//an extra method to increase the probability of egress if there are still TCs left 
		//pe set to double this time. 
		
		
		
		
	
	@ScheduledMethod(start = 1, interval = 1, priority = 9)
		public void runCalculations()
		{
			calculateSumMHCII();
			calculateBvVolumes(); 
			calculateRecruitmentRate();
			calculateInflux(); // the output Influx is then called within addCells()
		}
		public void calculateSumMHCII()
		{
			Object obj = getRandomObject();
			Context<Object>context =(Context)ContextUtils.getContext(obj);
			double totalMHCII = 0;
			double totalMHCI = 0;
			 for (Object dcs : context.getObjects(DC.class)) 
				{
					double MHCII = ((DC) dcs).getMHCII();
					totalMHCII = (totalMHCII + MHCII);
					double MHCI = ((DC)dcs).getMHCI();
					totalMHCI = (totalMHCI + MHCI);
				}
			 setTotalMHCII(totalMHCII);
			 setTotalMHCI(totalMHCI);
		}
		
		public void calculateBvVolumes()
		{
			//double outer = getOuterRadius();
			//double BvVolume = (((Math.pow((outer*6*Constants.ENTRY_PERCENTAGE),3.0)) * (Math.PI)*4/3)/2 );
			double BvVolume= (((Math.pow((hevRadiusEq*6),3.0)) * (Math.PI)*4/3)/2 );
		//	System.out.println("Bv 1 = "+BvVolume+"BV2="+BvVolumeTest);
			setBv(BvVolume);	
		//	System.out.println("hevRadiusEq"+hevRadiusEq);
		//	System.out.println("exitRadiusEq"+exitRadiusEq);
		//	System.out.println("Bv current = "+BvVolume);
		}

		public void calculateRecruitmentRate()
		{
			if (Constants.RecruitmentSwitch == 1)
			{				
					double value = getTotalMHCII();
					if (value > Constants.recruitmentT1 && value < Constants.recruitmentT2)
							{        //double recruitment = (0.00005*value*Constants.recruitmentFactor);// = 1.6 //previosuly 1.5
									
						//double recruitment = (1+(value*Constants.recruitmentFactor));
									double recruitment = (1+(value*Constants.recruitmentFactor));
									setRecruitment(recruitment);
							}
					else if (value>=Constants.recruitmentT2)
							{
								
							//	double recruitment = (1+(Constants.recruitmentT2*Constants.recruitmentFactor));
								double recruitment = (1+(Constants.recruitmentT2*Constants.recruitmentFactor));
								setRecruitment(recruitment);
							}
					else
							{		setRecruitment(1);}		
			}
			else 
				{} //recruitment is turned off
		}

		public void calculateInflux()
		{		double time = (RunEnvironment.getInstance().getCurrentSchedule().getTickCount());
				if (time<Constants.TICK_BV_RECRUITMENT_CHANGE){//{2160
				double TotalCellsToAdd = (double)((Constants.Initial_Tcellcount/ Constants.Tres));
				setInflux(TotalCellsToAdd);
				}
				if (time==Constants.TICK_BV_RECRUITMENT_CHANGE){//{2160
					LastAdd = (double)((Constants.Initial_Tcellcount/ Constants.Tres));
					double TotalCellsToAdd = (double)((Constants.Initial_Tcellcount/ Constants.Tres));
					setInflux(TotalCellsToAdd);
				}
				
				
				if (time>Constants.TICK_BV_RECRUITMENT_CHANGE)
				//but if > 4350 then add the ratio in //but to prevent this creeping unneccesarily maybe add a limit? 
				{  double ratio = getBv()/getInitialBv();
			//	System.out.println("BV Ratio is "+ ratio);
				   
				   
				   
						  if (Constants.RecruitmentSwitch ==0){
							  double TotalCellsToAdd = (double)((Constants.Initial_Tcellcount/ Constants.Tres))*ratio;
						//	  if (TotalCellsToAdd-LastAdd>3 || TotalCellsToAdd-LastAdd<1)																														
								  //{
									  setInflux(TotalCellsToAdd);}
								 // LastAdd = TotalCellsToAdd;}}
											
						  if (Constants.RecruitmentSwitch == 1)
						  {
							  double TotalCellsToAdd2 = ((double)((Constants.Initial_Tcellcount/ Constants.Tres))*ratio)*getRecruitment();
							 // if (TotalCellsToAdd2-LastAdd>3 || TotalCellsToAdd2-LastAdd<1)									  
							//  { LastAdd = TotalCellsToAdd2;
					//		  System.out.println("Recruitment = "+getRecruitment() +" Total Cells to add = " + TotalCellsToAdd2);
							  
							//  }
							  setInflux(TotalCellsToAdd2);}
						  //}
					}}
		
		// a method to calculate the way to fix the sigmoid curve so you don't need to recalculate 
	@ScheduledMethod(start = Constants.TICK_VOLUME_CHANGE-1, priority = 16)
		public void calculateSigmoidCorrection() {
			if ( Constants.maxSwelling== 1 || Constants.maxSwelling==55) 
		    { //do nothing : no expansion or 55% volume maintainence
			}
			if ( Constants.maxSwelling > 1 && Constants.maxSwelling <7 ) 
		    { 
				
					double slope = Constants.slope;//(Double)params.getValue("slope");
					double midpoint = Constants.midpoint; 
					double maxSwelling= Constants.maxSwelling;
					
					sigmoid_correction= maxSwelling/(1+(Math.exp(-slope*(getTCellCount()-midpoint))));
				
			}
			if ( Constants.maxSwelling >=7 && Constants.maxSwelling != 55 ) 
			{//error stop stimulation
				
				System.out.println("Max Swelling = unaccepted value , program terminating");
				System.out.println("Only values < 7 are accepted");
				//call the ending early thing
				copyResults.copy("endingEarly");
				RunEnvironment.getInstance().endRun();
			}
			
			if ( Constants.maxSwelling ==0 ) 
			{//error stop stimulation				
				System.out.println("Max Swelling = 0 = unaccepted value , program terminating");
				//call the ending early thing
				copyResults.copy("endingEarly");
				RunEnvironment.getInstance().endRun();
			}
		}
		
	
		
		@ScheduledMethod(start = Constants.TICK_VOLUME_CHANGE, interval = 10, priority = 11) //544
		public void alterGrid() { // need to alter this so that for the first 1 day + 600 s = 55% volume 
			// so
			
			
			if (Constants.maxSwelling== 1) 
			    { //do nothing : no expansion
				}
			    			
			if (Constants.maxSwelling== 55) //keeps volume at a constant 55% occupation
				{
					double newNodevolume = (getTCellCount()*150)/Constants.Occupation;
					varyvolume(newNodevolume);
				}
			if ( Constants.maxSwelling > 1 && Constants.maxSwelling <7) 
				{
					double initialVolume = initialV;//1.6257515787656121*Math.pow(10,7);//with the initial volume taken at equilibrium
					double slope = Constants.slope;
				    double midpoint =Constants.midpoint;									
					double maxSwelling= Constants.maxSwelling-1.0; //because eg. it's double the volume but actually a 1fold change
					double sigmoidFactor1 =  maxSwelling/(1+(Math.exp(-slope*(getTCellCount()-midpoint))));
					double sigmoidFactor = sigmoidFactor1-sigmoid_correction;
				//	if (sigmoidFactor < 0 ||sigmoidFactor>0.04)
					
					//{	
					double Nodevolume = (1+ sigmoidFactor) * initialVolume; 
					varyvolume(Nodevolume);
					//}
				}
			}
					
		public void varyvolume(double new_volume){//pass node volume in micrometers
			
			double Nodevolume = new_volume;
			double calculated_radius1 = ((6.0*Nodevolume)/(4.0*Math.PI));
			double calculated_radius=Math.pow(calculated_radius1,0.333333)/6;		
			double outer= calculated_radius;
		
			//store old values for different methods 
			double old_radius= getOuterRadius()*6;	
			double new_radius=outer*6;
			
			setOuterRadius(outer);	
			double new_inner = calculated_radius-2;
			setInnerRadius(new_inner);
		
			/// then choose your method to alter the geometry ( or move this to constants)	
			
			radiusMethod(outer);
		//	volumeMethod(new_volume,outer);
		//	hevRadiusMethod(outer, old_radius, new_radius);
		//	exitRadiusMethod(outer, old_radius, new_radius);
		//	exit_HEV_Method(outer, old_radius, new_radius);
		}			

		public void radiusMethod(double outer)	{			
			double exit_radius = exit_percent * outer; // these are in grids not micrometers
			double entry_radius = entry_percent * outer;
			setHEVRadiusEq(entry_radius); // to update initialising HEVs (instead of calculating again)
			setExitRadiusEq(exit_radius);
			setGeometry(outer,exit_radius,entry_radius);
		//	System.out.println("Exit radius real"+ exit_radius );
		//	System.out.println("Outer"+outer);
		//	System.out.println("Entry rad real" + entry_radius);
			}
			
		
		public void volumeMethod(double new_volume,double outer){ // maybe need to pass in variable here of the old entry radius. 
			//probably need to store and old radius and old entry thing, which i may do already
			//double time = (RunEnvironment.getInstance().getCurrentSchedule().getTickCount());
			//if (time > Constants.TICK_AREAS_RADIUS_CHANGE){
			//want to calculate the initial percent
			double iv=initialV; // these are half node //have the values reinitialise one step before this?
			double v=new_volume;
			double change = v/iv;
	//		System.out.println("volume change calc vol = "+ change);
			
			//double old_radius_HEV=getHEVRadiusEq()*6; // should be getting the initialBV not the last step
			//note that initial BV and current BV ARE the hsalf node vlaues so should be doubled before 
			//calculating radius
			//double old_radius_HEV=getInitialBv();
		//	double old_HEV_volume= getInitialBv();//(((Math.pow(old_radius_HEV,3.0)) * (Math.PI)*4/3)/2); //this is a half
			double newHEVvolume= getInitialBv()*change*2;
			double newHEVradius1=((3*newHEVvolume)/(4*Math.PI));
			double newHEVradius=Math.pow(newHEVradius1,0.333333)/6;	
			double entry_radius=newHEVradius;
			setHEVRadiusEq(entry_radius);
	//		System.out.println("entry radius calc vol = "+ entry_radius);

			//exit is then determined by setting the non exit area proportional to the paracortex voume
	//		double old_radius_exit=getExitRadiusEq()*6;// no this should be get intiial!Reinitialse at BV 
			double old_exit_volume= initialPV;//(((Math.pow(old_radius_exit,3.0)) * (Math.PI)*4/3)/2); //this is a half
			double newParavolume= old_exit_volume*change*2;
			double newPararadius1=((3*newParavolume)/(4*Math.PI));
			double newPararadius=Math.pow(newPararadius1,0.333333)/6;	
			double exit_radius=newPararadius;
			
			
			setExitRadiusEq(exit_radius);
		//	System.out.println("exit radius calc vol = "+ exit_radius);
			setGeometry(outer,exit_radius,entry_radius);}	
		//	else
		//	{radiusMethod(outer);}
			
		//}
	    
			
			
		public void exitRadiusMethod(double outer, double old_radius, double new_radius)
			{
				//double time = (RunEnvironment.getInstance().getCurrentSchedule().getTickCount());
				//if (time > Constants.TICK_AREAS_RADIUS_CHANGE){
				double entry_radius = entry_percent * outer;	//  ** If both section C and section D used , comment out this line		
				setHEVRadiusEq(entry_radius); 
				double old_radius_interm=getExitRadiusEq()*6; //what is this? i think it gets the original radius
			    double rate_change_entry_outer = old_radius-new_radius;
				double a= 2*Math.PI;
				double b= -(old_radius_interm * 2*Math.PI);
				double c =0;
				double d = -(rate_change_entry_outer);
				Cubic cubic = new Cubic();
			    cubic.solve(a, b, c, d);
			//  System.out.println("x1 = " + cubic.x1);
				double x =cubic.x1; 
				double exit_radius = x/6;
		//		System.out.println("Exit radius= " + exit_radius);
		//		System.out.println("Outer radius= " + outer);
				setExitRadiusEq(exit_radius);
				
				setGeometry(outer,exit_radius,entry_radius);} // if you want to vary both then that will be a different method 						
			//	else
			//	{radiusMethod(outer);}
			//}
			
			
		public void hevRadiusMethod(double outer, double old_radius, double new_radius)
			{
		//	double time = (RunEnvironment.getInstance().getCurrentSchedule().getTickCount());
		//	if (time > Constants.TICK_AREAS_RADIUS_CHANGE) //2300 ( after BV recruitment changes to allow stabilisation, and before DCs enter
			//{				
				double exit_radius = exit_percent * outer;//** If both section 1 and section 2 used , comment out this line
				double old_radius_HEV=getHEVRadiusEq()*6; // so we do keep a track of the old radius
			    double rate_change_entry_outer = old_radius-new_radius;
				double a2= 2*Math.PI;
				double b2= -(old_radius_HEV * 2*Math.PI);
				double c2 =0;
				double d2 = -(rate_change_entry_outer);
				Cubic cubic2 = new Cubic();
				cubic2.solve(a2, b2, c2, d2);
				//System.out.println("x1 = " + cubic.x1);
				double x2 =cubic2.x1; 
				double entry_radius = x2/6;
				setHEVRadiusEq(entry_radius); //is this the error? exit_radius
				setGeometry(outer,exit_radius,entry_radius);		
			//	System.out.println("Exit radius= " + exit_radius);
			//	System.out.println("Outer radius= " + outer);
			//	System.out.println("Entry radius= " + entry_radius);
		//	}
		//	else
		//	{radiusMethod(outer);}		
			}	
			
//method if you want the HEV to grow at same outer rate and exit spread more thinly. 
			public void exit_HEV_Method(double outer, double old_radius, double new_radius){
				//combination of the two, firstly calculating HEV then Exit 
				//double time = (RunEnvironment.getInstance().getCurrentSchedule().getTickCount());
				//if (time > Constants.TICK_AREAS_RADIUS_CHANGE){
				
				//first HEV
				double old_radius_HEV=getHEVRadiusEq()*6;
			    double rate_change_entry_outer = old_radius-new_radius;
				double a2= 2*Math.PI;
				double b2= -(old_radius_HEV * 2*Math.PI);
				double c2 =0;
				double d2 = -(rate_change_entry_outer);
				Cubic cubic2 = new Cubic();
				cubic2.solve(a2, b2, c2, d2);
				double x2 =cubic2.x1; 
				double entry_radius = x2/6;
				setHEVRadiusEq(entry_radius); //is this the error? exit_radius
				
				//then exit
				double old_radius_interm=getExitRadiusEq()*6; //what is this? i think it gets the original radius
				double a= 2*Math.PI;
				double b= -(old_radius_interm * 2*Math.PI);
				double c =0;
				double d = -(rate_change_entry_outer);
				Cubic cubic = new Cubic();
			    cubic.solve(a, b, c, d);
				double x =cubic.x1; 
				double exit_radius = x/6;
				setExitRadiusEq(exit_radius);	
				
				//set both
				setGeometry(outer,exit_radius,entry_radius);
		//		System.out.println("Exit radius= " + exit_radius);
		//			System.out.println("Outer radius= " + outer);
		//			System.out.println("Entry radius= " + entry_radius);
			//	}
			//	else {radiusMethod(outer);}
				
			};
		
		
		
		
		
		
//to try and reduce interations over grid,s		
		public void setGeometry(double outer,double exit_radius,double entry_radius)
		{
			Grid grid = (Grid)getProjection("grid");
			GridValueLayer geometrylayer = (GridValueLayer)getValueLayer("Geometry");	
			double exit_height = (0.75 * outer) + half_world; //75% of the height of the node
			 for (int i =0; i < gridWidth; i++){
				for (int j = 0; j < gridHeight; j++){
					for (int k = 0; k < gridz; k++){
				
						GridPoint centre = new GridPoint(centreX,centreP,centreP);
						GridPoint otherPoint= new GridPoint(i,j,k);
						//boundary
						if (grid.getDistance(otherPoint,centre) < outer)//outer_radius
						{geometrylayer.set(15,i, j,k);}
						//outside
						if (grid.getDistance(otherPoint,centre) > outer) //outside node
						{geometrylayer.set(0,i, j,k);}
						//then exit /entry
						if (grid.getDistance(otherPoint,centre) < new_inner) 
						{geometrylayer.set(20,i, j,k);}
						// then exit area 
						if (grid.getDistance(otherPoint,centre) < new_inner && j < exit_height)  
						{geometrylayer.set(18,i, j,k);}
						//then normal area
						if (grid.getDistance(otherPoint,centre)  < exit_radius )
						{geometrylayer.set(20,i, j,k);}
						//then the inside
						if (grid.getDistance(otherPoint,centre) < entry_radius) //inner_radius
						{geometrylayer.set(24,i, j,k);}
					}}}
		}
	// method to update that cross section
		
	//	@ScheduledMethod(start=1,interval = 30, priority = 18)
	//	public void updateCrossSection()
	//	{
			// take the middle cross section and convert to a 2 D grid
	//		Grid grid = (Grid)getProjection("grid");
	//		GridValueLayer geometrylayer = (GridValueLayer)getValueLayer("Geometry");	
	//		GridValueLayer crossSectionLayer = (GridValueLayer)getValueLayer("CrossSection");	
	//		for (int j=0; j<gridHeight; j++){
	//			 for (int k = 0; k < gridz; k++){
		//			 double v1 = geometrylayer.get(1,j,k);
		//			 crossSectionLayer.set(v1,j,k);}}
	//	}
			
		
		
		
		
		
		@ScheduledMethod(start=1,interval = 180, priority = 17)
        public void countGrids(){
            Grid grid = (Grid)getProjection("grid");
            GridValueLayer geometrylayer = (GridValueLayer)getValueLayer("Geometry");
            int count = 0;
            for (int i=0; i<gridWidth; i++){
                for (int j=0; j<gridHeight; j++){
                     for (int k = 0; k < gridz; k++){              
                    	 if (geometrylayer.get(i, j,k)>1)// this includes boundary, exit and entry
                    		 // i think this will work if not do the getPoint. then N.getX()
                            {count++;}
                }}}     
            setGridNumber(count);          
        }
	
		//comment out if you don't want this 
		
	//	@ScheduledMethod(start=1,interval = 1, priority = 18)
		public void aSecondCrowding() throws IOException{
			 Grid grid = (Grid)getProjection("grid");
	            GridValueLayer geometrylayer = (GridValueLayer)getValueLayer("Geometry");
	            int count = 0;
	            for (int i=0; i<gridWidth; i++){
	                for (int j=0; j<gridHeight; j++){
	                     for (int k = 0; k < gridz; k++){    
	                    	 
	                        	 if (geometrylayer.get(i, j,k)==18){
	                        		 GridPoint pt = new GridPoint(i,j,k);	                        	 
	                        		 GridCell<Tcell> cell = new GridCell<Tcell>(pt,Tcell.class);
	                        		 double size= cell.size();
	                        		if (size>1.0)
	                        		    {System.out.println("Two t cells in the grid size: "+size);
	                        		     for (Tcell TC : cell.items() ){
	                        		    	 if( TC.getRetention()==0)
	                        		    	 {TC.seeIfExit();}
	                        		    	 
	                        		     }}}
	                    	 //make grid cell here 
	                    	 // check size 
	                    	 //if size > 1
	                    	 //list agents here= grab agents
	                    	 //tc.seeIfexit.
	                     }}}
		}
		
		
			 	 
		@ScheduledMethod(start=(Constants.TICK_DCS_ENTER-1), priority = 6)
		public void calculateDCsToAdd()
		{
			// this remains a set rate for a set time period (2.5days)
			int interval = Constants.DC_ADDITION_INTERVAL;
			int timeperiod=Constants.DC_ADDITION_TIME;
			int numberofintervals = timeperiod/interval;
			System.out.println("number of intervals: "+ numberofintervals);
			System.out.println("Total num DCs to add: "+ getDCnumber() );
			DCsToAdd=Math.round(getDCnumber()/numberofintervals);
			System.out.println("DCs to add per interval"+ DCsToAdd);
		}
		
		@ScheduledMethod(start=Constants.TICK_DCS_ENTER,interval = Constants.DC_ADDITION_INTERVAL, priority = 6)
		public void toAddDC()
			{
			
			double time = (RunEnvironment.getInstance().getCurrentSchedule().getTickCount());
			int additiontime= Constants.TICK_DCS_ENTER+Constants.DC_ADDITION_TIME;

				if (time < additiontime)
					{   setDCnumber(getDCnumber()-DCsToAdd); //update DC counts
						Object obj = getRandomObject();
						int index = 0;
						Grid grid = (Grid)getProjection("grid");
						SimUtilities.shuffle(FreePoints,RandomHelper.getUniform()); //freepoints = pre made list
						while(index<DCsToAdd ) 
						{
							GridPoint pt = FreePoints.get(index);
							int retainTime = 0;
							double value =  Constants.MHCstartvalue;//(Double)params.getValue("MHCstartingvalue");
							double MHCI= RandomHelper.createNormal(value, 5).nextDouble();;//0;
							double MHCII= RandomHelper.createNormal(value, 5).nextDouble();;//0;
							int boundCount= 0;
							int TcellsContacted = 0;
							int cogTcellsContacted = 0;
							int age2 = 1;
							boolean licenced = false;
							DC DCcell = new DC (grid,retainTime,MHCI,MHCII,boundCount,TcellsContacted,cogTcellsContacted,age2,licenced);
							Context<Object>context =(Context)ContextUtils.getContext(obj);
							context.add(DCcell); 
							//space.moveTo(DCcell,Position.getX(),Position.getY(),Position.getZ());//space pt
							grid.moveTo(DCcell,pt.getX(),pt.getY(),pt.getZ());//grid pt
							DCCellCount++;
							index++;
						}
					}
						else
						{} //stop adding
			}
		
	@ScheduledMethod(start = 10, interval = 1,priority = 5)
		public void cellEntry(){
		double TotalCellsToAdd=getInflux();
	//	System.out.println("Tcells to add"+TotalCellsToAdd);
		//calculate how many T cells to add from the HEVs and the afferent lymphatics
			float cellsToAddHEV1 = Math.round((TotalCellsToAdd * (Constants.fract_HEV)));	
			int cellsToAddHEV = Math.round(cellsToAddHEV1);
			float cellsToAddAff1 = Math.round((TotalCellsToAdd)* (Constants.fract_aff));
			int cellsToAddAff = Math.round(cellsToAddAff1);		
			int entrycount = getTCellsEntered() + cellsToAddHEV + cellsToAddAff;
			setCountTCellsEntered(entrycount);		
			Object obj = getRandomObject();
			int index = 0;
			//HEV grid areas
			List<GridPoint>EntryList = new ArrayList<GridPoint>();
			Grid grid = (Grid)getProjection("grid");
			GridValueLayer geometrylayer = (GridValueLayer)getValueLayer("Geometry");
			for (int i =0; i < gridWidth/2; i++){
			for (int j = 0; j < gridHeight; j++){
				for (int k = 0; k < gridz; k++){
			if(geometrylayer.get(i,j,k) == 24){
			GridPoint point = new GridPoint(i,j,k);
			
			EntryList.add(point);
			}}}}
			//can filter them to check they are empty if wanted //POINT 2 TO FIX //shuffle the list
			SimUtilities.shuffle(EntryList, RandomHelper.getUniform());
			//create cells and add to context
			while(index<cellsToAddHEV ) 
			{				
			GridPoint pt = EntryList.get(index);
			int retain_time = 0;
			int age = 1;
			int timeSinceLastBound = 0;
			int timeSinceEntered = 1; //different to old cells and daughter cells
			int DCContacted = 1; //different to old cells
			double S1P1 = Constants.S1P1postEntry; // set at this so they don't exit straight away ( down regulated in the blood)
			int istring = 0;
			boolean trackingvalue=false;

			if (RandomHelper.nextIntFromTo(0, 1000000) < (Constants.cognition * 1000000))// set at 10 so 0.1% cognate
			{ 
				makeCognateCell(pt);//,Position
				Tcell_count++;	
			}
					
			else 
			{
			UncognateCell cell = new UncognateCell(
					//space,
					grid,retain_time,age,
					timeSinceLastBound,
					timeSinceEntered,
					DCContacted,
					S1P1,
					istring,
					trackingvalue
					);	
			Context<Object>context =(Context)ContextUtils.getContext(obj);
			context.add(cell); 
			grid.moveTo(cell,pt.getX(),pt.getY(),pt.getZ());//grid pt
			
			//add a time stamp to this so it collects once the model has settled slightly
			double time = (RunEnvironment.getInstance().getCurrentSchedule().getTickCount());
			if (time > Constants.timeTostartTracking){
				
			if (Constants.TrackingSwitch ==1)
				{if (celltrackcounter > 0 && celltrackcounter <=Constants.numTcellsToTrack)
				{cell.setIstring(celltrackcounter);
				cell.setTracking(true);}
				celltrackcounter++;}
			}
			
			Tcell_count++;}
			index++;
			}
			if (cellsToAddAff>0){
			addCellsAfferent(cellsToAddAff);
			}
		}
	
		public void addCellsAfferent (int cells)
		{
			Object obj = getRandomObject();
			int cellsAff = cells;
			int index1 = 0;
			List<GridPoint>EntryListAff = new ArrayList<GridPoint>();
			Grid grid = (Grid)getProjection("grid");
			GridValueLayer geometrylayer = (GridValueLayer)getValueLayer("Geometry");
			//make the list of places to add ( in the afferet lymphatic area)
			//currently this alters with size, would be more computationally efficient to not
			int ilow = 0;
			int ihigh = (int)(0.3*inner_radius);
			int zlow = (int)(gridz/2 - (0.3 * inner_radius));
			int zhigh = (int)(gridz/2 + (0.3*inner_radius));
			for (int i =ilow; i < ihigh; i++){
				for (int j = gridHeight/2; j < gridHeight; j++){
					for (int k = zlow; k<zhigh;k++)				
						{if(geometrylayer.get(i,j,k) == 20 ){
							GridPoint point = new GridPoint(i,j,k);
							EntryListAff.add(point);}}}};
			SimUtilities.shuffle(EntryListAff, RandomHelper.getUniform());
			
			while(index1<cellsAff ) 
			{
			GridPoint pt = EntryListAff.get(index1);
			int retain_time = 0;
			int age = 1;
			int timeSinceLastBound = 0;
			int timeSinceEntered = 1; //different to old cells and daughter cells
			int DCContacted = 1; //different to old cells
			double S1P1 = Constants.S1P1postEntry; // initially low, reupregulated after 45min-2h
			int istring = 0;
			boolean trackingvalue = false;
			//make cognate cells set at 10 so 0.1% cognate which need to have a cd4 and cd8 attribute
			if (RandomHelper.nextIntFromTo(0, 1000000) < (Constants.cognition * 1000000)){
			makeCognateCell(pt);//,Position
			Tcell_count++;		
			}
			else 
			{
	//make uncognate cells that these are not divided into CD4/CD8
			UncognateCell cell = new UncognateCell(
				//	space,
					grid,
					retain_time,
					age,
					timeSinceLastBound,
					timeSinceEntered,
					DCContacted,
					S1P1,
					istring,
					trackingvalue
					);	
			Context<Object>context =(Context)ContextUtils.getContext(obj);
			context.add(cell); 
			grid.moveTo(cell,pt.getX(),pt.getY(),pt.getZ());
			Tcell_count++;
				double time = (RunEnvironment.getInstance().getCurrentSchedule().getTickCount());
				if (time > Constants.timeTostartTracking){
				if (Constants.TrackingSwitch ==1){
					if (celltrackcounter > 0 && celltrackcounter <=Constants.numTcellsToTrack)
						{cell.setIstring(celltrackcounter);
					cell.setTracking(true);}
					celltrackcounter++;
				}}}
			index1++;
			}	
		}

		public void makeCognateCell(GridPoint pt)
		{
			Object obj = getRandomObject();
			Grid grid = (Grid)getProjection("grid");
			int retain_time = 0;
			int age = 1;
			int timeSinceFirstBind =0;
			int timeSinceLastBound = 0;
			boolean activated = false;
			boolean effector = false;
			double stimulation = 0;
			int profCount = 0;
			double S1p1 = Constants.S1P1postEntry;// initially low, reupregulated after 45min-2h
			int istring=0;
			boolean trackingvalue=false; //this is later reset
			int timeAct = 0;
			int timeSinceEntered = 1; //different to old cells and daughter cells
			int DCContacted = 1; //different to old cells
			boolean M = false;
			int TimeSinceDif = 0;			
			if(RandomHelper.nextIntFromTo(0, 100) < (Constants.RatioCD8 * 100)){
			boolean CD4 =false;
			boolean CD8 =true;
			CognateCell cell = new CognateCell (
				grid,	
				retain_time,
				age,
		       timeSinceLastBound,
		       timeSinceEntered,
		       DCContacted,
		       timeSinceFirstBind,
		       stimulation,
		       activated,
		       effector,
		       profCount,
		       S1p1,
		       istring,
		       trackingvalue,
		       timeAct,
		       CD4,
		       CD8,
		       M,
		       TimeSinceDif
		       );
		Context<Object>context =(Context)ContextUtils.getContext(obj);
		context.add(cell); 
		grid.moveTo(cell,pt.getX(),pt.getY(),pt.getZ());//grid pt
		
				double time = (RunEnvironment.getInstance().getCurrentSchedule().getTickCount());
				if (time > Constants.timeTostartTracking){
				if (Constants.TrackingSwitch ==1){
					if (celltrackcounter > 0 && celltrackcounter <=Constants.numTcellsToTrack)
							{cell.setIstring(celltrackcounter);
					cell.setTracking(true);}
					celltrackcounter++;
					}	}
			}
			else
				{
			boolean CD4 =true;
			boolean CD8 =false;
			CognateCell cell = new CognateCell (
				grid,			
				retain_time,
				age,
		       timeSinceLastBound,
		       timeSinceEntered,
		       DCContacted,
		       timeSinceFirstBind,
		       stimulation,
		       activated,
		       effector,
		       profCount,
		       S1p1,
		       istring,
		       trackingvalue,
		       timeAct,
		       CD4,
		       CD8,
		       M,
		       TimeSinceDif
		       );
		Context<Object>context =(Context)ContextUtils.getContext(obj);
		context.add(cell); 
		grid.moveTo(cell,pt.getX(),pt.getY(),pt.getZ());//grid pt
		double time = (RunEnvironment.getInstance().getCurrentSchedule().getTickCount());
		if (time > Constants.timeTostartTracking){
			if (Constants.TrackingSwitch ==1){
				if (celltrackcounter > 0 && celltrackcounter <=Constants.numTcellsToTrack)
					{cell.setIstring(celltrackcounter);
				cell.setTracking(true);}
				celltrackcounter++;}
				}	}
		}
		
	//removal method when T cells exit the lymph node
	public static void removeCellExit(Tcell cell) throws IOException {
			Object byebye = cell;
			int time_value = ((Tcell) byebye).getTimeSinceEntered();
			if (time_value > 0){		
					//to print transit time of all cells use 
					printdata.printer1(time_value);		
					Context<Object> context = (Context)ContextUtils.getContext(byebye);
					context.remove(byebye); 
					Tcell_count--;
					count_of_Tcells_exited++; 
					//put in exit as the old cells use this code too and don't exit?
					if (byebye instanceof CognateCell)
						{
							cog_exited++;
							printdata.printer2(time_value);//of just cognate cells	
							if(((CognateCell)byebye).getActivation()==true)
								{
									active_exited++;	
									printdata.printer3(time_value); //activated
								}
							if (((CognateCell) byebye).getEffector()==true)
								{ 	eff_exited++;
									printdata.printer4(time_value); //effectors
								}
							if (((CognateCell) byebye).getM()==true)
								{ memory_exited++;
									printdata.printer5(time_value); //memory
								}
				
							//the bit for separating memory and effector into cd4 and cd8 exited	
							if (((CognateCell) byebye).getCD4()==true)
								{
									if (((CognateCell) byebye).getActivation()==true)
										{ active_exited4++;}
									if (((CognateCell) byebye).getEffector()==true)
										{ eff_exited4++;}
									if (((CognateCell) byebye).getM()==true)
										{ memory_exited4++;}
										cog_exited4++;
								}
							else 
							{
									if (((CognateCell) byebye).getActivation()==true)
										{ active_exited8++;}
									if (((CognateCell) byebye).getEffector()==true)
										{ eff_exited8++;}
									if (((CognateCell) byebye).getM()==true)
										{ memory_exited8++;}
										cog_exited8++;	}		
							} // end of the if loop for cognate cells 
			
			else
				{	 
				// so this is just so i can record the transit of normal non cognate cells after letting them 
				//settle for a couple of hours
				double time = (RunEnvironment.getInstance().getCurrentSchedule().getTickCount());
				if (time > 3000 && time <8000)
				{printdata.printer6(time_value);} //this will be transit time of not cognate cells only 
				}			}				
			else
				// to catch anything i accidentally missed, eg. and original cell
				//exiting ( will have time since_entered=0) but actually this shoudn't really catch anything
			{
				Context<Object> context = (Context)ContextUtils.getContext(byebye);
				context.remove(byebye); 
				Tcell_count--;
				count_of_Tcells_exited++; 
				//there isn't a count for non cog exit as you can work this out from total -cog
				//and there isn't a print transit here because don't really want the transit of 
				// a cell already in the LN. 
				if (byebye instanceof CognateCell)
				{
					cog_exited++;
					
					if(((CognateCell)byebye).getActivation()==true)
					{
						active_exited++;	
					}
					if (((CognateCell) byebye).getEffector()==true)
					{ eff_exited++;
					
					}
					if (((CognateCell) byebye).getM()==true)
					{ memory_exited++;}
					
				//Counts for separate CD4/CD8 memory and effector 	
				if (((CognateCell) byebye).getCD4()==true)
				{
					if (((CognateCell) byebye).getActivation()==true)
					{ active_exited4++;}
					if (((CognateCell) byebye).getEffector()==true)
					{ eff_exited4++;}
					if (((CognateCell) byebye).getM()==true)
					{ memory_exited4++;}
					 cog_exited4++;
				}
				else 
				{
					if (((CognateCell) byebye).getActivation()==true)
					{ active_exited8++;}
					if (((CognateCell) byebye).getEffector()==true)
					{ eff_exited8++;}
					if (((CognateCell) byebye).getM()==true)
					{ memory_exited8++;}
					 cog_exited8++;
				}}}		
		}

	//removal method for cells when dying of old age
		public static void removeCellAgeing(Tcell cell)
		{
			Object byebye = cell;
			Context<Object> context = (Context)ContextUtils.getContext(byebye);
			context.remove(byebye); 
			Tcell_count--;
		}

		public static void removeDC(DC DC) throws IOException {
			Object byebye = DC;
			//take information regarding T cells contacted before death
			int TcellsContacted = ((DC)byebye).getTcellsContacted();
			int cogTcellsContacted = ((DC)byebye).getcogTcellsContacted();
			ArrayList list = new ArrayList();
			list.add(TcellsContacted);
			list.add(cogTcellsContacted);
			String filename="./TcellsContacted.csv";
			PrintTcellsContacted.printTcsCont(filename,list);
			list.clear();
			Context<Object> context = (Context)ContextUtils.getContext(byebye);
			context.remove(byebye); 
			DCCellCount--;		
		}
		
		
		
		@ScheduledMethod(start = 13800,priority = 5)
		public void printDCsContacted() throws IOException{
			//basically want a list of Tcells : DCs contacted : cognate T cells contacted
			//but we don't want 50000 cells, maybe we should stick to just 100 new cells, 
			//or like within the first 200 timesteps?
			Object ob1 = getRandomObject();
			Context<Object>context =(Context)ContextUtils.getContext(ob1);
			ArrayList<Integer>  lise = new ArrayList();
			for (Object obj:context){

				if (obj instanceof Tcell)
				{
					int time_e = ((Tcell) obj).getTimeSinceEntered();
					int DCscontacted= ((Tcell) obj).getDCContacted();
					String filename = "./DClist.csv";		
					if (obj instanceof CognateCell)
					{lise.add(1);}
					else
					{lise.add(0);}
					lise.add(time_e);
					lise.add(DCscontacted);
					PrintDCsContacted.printDcsCont(filename, lise);
					lise.clear();		
				}}			
		}
	
		@ScheduledMethod(start = 30240,priority = 5)
		public void printProf() throws IOException{
			//basically want a list of Tcells : DCs contacted : cognate T cells contacted
			//but we don't want 50000 cells, maybe we should stick to just 100 new cells, 
			//or like within the first 200 timesteps?
			Object ob1 = getRandomObject();
			Context<Object>context =(Context)ContextUtils.getContext(ob1);
			ArrayList<Integer>  lise = new ArrayList();
			for (Object obj:context){

				if (obj instanceof CognateCell)
				{
					int time_e = ((CognateCell) obj).getTimeSinceEntered();
					int profcount= ((CognateCell) obj).getProfCount();
					if (((CognateCell) obj).getCD4() == true)
							{int cd4=1;
							lise.add(cd4);}
					else
							{int cd4=0;
							lise.add(cd4);}
				
					String filename = "./Proflist.csv";		
					lise.add(time_e);
					lise.add(profcount);
					printProf.printProlifs(filename, lise);
					lise.clear();		
				}}			
		}
		
		
	//end of main methods

		public void setInnerRadius(double new_inner)
		{
		this.new_inner = new_inner;
		}

		public double getInnerRadius(){
		return new_inner;
		}

		public static void setTCellCount(int value){
		Tcell_count = value;
		}

		public static int getTCellCount()
		{
		    return Tcell_count;
		}

		public static int getDCCellCount()
		{
		    return DCCellCount;
		}

		public static int getCellExitCount()
		{
		    return count_of_Tcells_exited;
		}
		public static void setCellExitCount(int value)
		{
		    count_of_Tcells_exited = value;
		}

		public static int getTCellsEntered()
		{
			return count_of_Tcells_entered;	
		}

		public static void setCountTCellsEntered(int value)
		{
			count_of_Tcells_entered = value;
		}

		public void setTotalMHCII(double value)
		{
			lymph_node3DContext.totalMHCII = value;
		}
		public static double getTotalMHCII()
		{
		return totalMHCII;
		}
		public static double getTotalMHCI()
		{
		return totalMHCI;
		}
		public void setTotalMHCI(double value)
		{
			lymph_node3DContext.totalMHCI= value;
		}

		public void setBv(double value)
		{
			this.Bv = value;
		}
		public double getBv()
		{
			return Bv;
		}

		public void setOuterRadius(double value)
		{
			this.outerRadius = value;
		}
		public double getOuterRadius()
		{
			return outerRadius;
		}

	//tracking section

		public int getIDrandom()
			{
				return Constants.randomnumber;
			}
		
			public double getInitialVolume()
			{
				return initialV;
			}
			public double getInitialBv()
			{
				return initialBV;
			}
			public double getInitialEv()
			{
				return initialBV;
			}
			public double getRecruitment()
			{
				return recruitment;
			}
			public void setRecruitment(double value)
			{
				recruitment = value;
			}
			public void setVolume(double value)
			{
				this.volume = value;
			}
			public double getVolume()
			{
				return volume;
			}
			public void setInflux(double value)
			{
				Influx = value;
			}
			public double getInflux()
			{
				return Influx;
			}
		
		//counters > quite a big section
		//can comment these out if not using cell counters

		public static int getCellCountCD4()
		{
			return cell_count_cd4;
		}
		public void setCellCountCD4(int value)
		{
			cell_count_cd4=value;
		}
		public static int getCellCountCD8()
		{
			return cell_count_cd8;
		}
		public void setCellCountCD8(int value)
		{
			cell_count_cd8=value;
		}
		public static int getCellCountM()
		{
			return cell_count_M;
		}
		public void setCellCountM(int value)
		{
			cell_count_M=value;
		}
		public static int getCellCountA()
		{
			return cell_count_A;
		}
		public void setCellCountA(int value)
		{
			cell_count_A=value;
		}
		public static int getCellCountE()
		{
			return cell_count_E;
		}
		public void setCellCountE(int value)
		{
			cell_count_E=value;
		}



		//extra cd4 and cd8 section

		//cd4 

		public static int getCellCountCD4_A()
		{
			return cell_count_cd4_A;
		}
		public void setCellCountCD4_A(int value)
		{
			cell_count_cd4_A=value;
		}

		public static int getCellCountCD4_E()
		{
			return cell_count_cd4_E;
		}
		public void setCellCountCD4_E(int value)
		{
			cell_count_cd4_E=value;
		}
		public static int getCellCountCD4_M()
		{
			return cell_count_cd4_M;
		}
		public void setCellCountCD4_M(int value)
		{
			cell_count_cd4_M=value;
		}


		//cd8

		public static int getCellCountCD8_A()
		{
			return cell_count_cd8_A;
		}
		public void setCellCountCD8_A(int value)
		{
			cell_count_cd8_A=value;
		}

		public static int getCellCountCD8_E()
		{
			return cell_count_cd8_E;
		}
		public void setCellCountCD8_E(int value)
		{
			cell_count_cd8_E=value;
		}
		
		public static int getCellCountCD8_M()
		{
			return cell_count_cd8_M;
		}
		public void setCellCountCD8_M(int value)
		{
			cell_count_cd8_M=value;
		}
		
		public static int getActiveExited()
		{
			return active_exited;
		}
		public void setActiveExited(int value)
		{
			active_exited=value;
		}
		
		public static int getEffExited()
		{
			return eff_exited;
		}
		public void setEffExited(int value)
		{
			eff_exited=value;
		}
		//
		public static int getCogExited()
		{
			return cog_exited;
		}
		public void setCogExited(int value)
		{
			cog_exited=value;
		}
	//
		public static int getMemoryExited()
		{
			return memory_exited;
		}
		public void setMemoryExited(int value)
		{
			memory_exited=value;
		}
		// The CD4 version
		public static int getActiveExited4()
		{
			return active_exited4;
		}
		public void setActiveExited4(int value)
		{
			active_exited4=value;
		}
		public static int getEffExited4()
		{
			return eff_exited4;
		}
		public void setEffExited4(int value)
		{
			eff_exited4=value;
		}
		public static int getMemoryExited4()
		{
			return memory_exited4;
		}
		public void setMemoryExited4(int value)
		{
			memory_exited4=value;
		}
		public static int getCogExited4()
		{
			return cog_exited4;
		}
		public void setCogExited4(int value)
		{
			cog_exited4=value;
		}
		// The CD8 version
			public static int getActiveExited8()
			{
				return active_exited8;
			}
			public void setActiveExited8(int value)
			{
				active_exited8=value;
			}
			public static int getEffExited8()
			{
				return eff_exited8;
			}
			public void setEffExited8(int value)
			{
				eff_exited8=value;
			}
			public static int getMemoryExited8()
			{
				return memory_exited8;
			}
			public void setMemoryExited8(int value)
			{
				memory_exited8=value;
			}
			public static int getCogExited8()
			{
				return cog_exited8;
			}
			public void setCogExited8(int value)
			{
				cog_exited4=value;
			}
			public double getExitRadiusEq()
			{
				return exitRadiusEq;
			}
			public void setExitRadiusEq(double value)
			{
				exitRadiusEq=value;
			}
			public double getHEVRadiusEq()
			{
				return hevRadiusEq;
			}
			public void setHEVRadiusEq(double value)
			{
				hevRadiusEq=value;
			}
			
			
			
			
			//public void setCogS1P1(int value)
			//{
				//this.cogS1P1reg = value;
			//}
			
		//	public int getCogS1P1()
			//{
				//return AllS1P1;
			//}
	//this is the number to add not the number present
		public int getDCnumber()
		{
			return DCnumber;
		}
		public void setDCnumber(int value)
		{
			DCnumber=value;
		}
		
		public int getGridNumber()
		{
			return gridNumber;
		}
		public void setGridNumber(int value)
		{
			gridNumber=value;
		}
		


		

	}