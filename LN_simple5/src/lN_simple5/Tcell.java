package lN_simple5;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.grid.CellAccessor;
//import repast.simphony.space.SpatialMath;
//import repast.simphony.space.continuous.ContinuousSpace;
//import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.SimUtilities;
import repast.simphony.valueLayer.GridValueLayer;

public class Tcell {
	 // variables for the cell
 //   protected ContinuousSpace<Object> space;
    protected Grid<Object> grid; //this was Grid<heatbug> grid;
    public int retainTime;
    public int age;
    public int timeSinceLastBound;
    public int timeSinceEntered;// because a DC is going to access and change it 
    public int DCContacted;// comment out if not required // updated on relases. DC.updateprojection
    public double S1P1;
    public int istring;
    public boolean trackingvalue;
    
//constructor   
    public Tcell (
    		//ContinuousSpace<Object>space,
    		Grid<Object>grid,   		
    		int retainTime,
    		int age,
    		int timeSinceLastBind,
    		int timeSinceEntered,
    		int DCContact,
    		double S1P1,
    		int istring,
    		boolean trackingvalue)
    {
        //this.space = space;
        this.grid = grid;
        this.retainTime = retainTime;
        this.age = age;
        this.timeSinceLastBound = timeSinceLastBind;
        this.timeSinceEntered = timeSinceEntered;
        this.DCContacted = DCContact;
        this.S1P1=S1P1;
        this.istring=istring;
        this.trackingvalue= trackingvalue;
    }
    
    
    @ScheduledMethod(start = 1, interval = 1 , priority = 8)
    public void StartUpdate() throws IOException
    {
    	//updating All cells > subclassesupdate with -2 
        int newage = getAge() + 1;
        setAge(newage);
        timeSinceLastBound++;
        if (retainTime > 0 ){retainTime--; } // or other way around
         
        // all cells can regulate S1P1 in response to inflammation> to remove this comment out this line           
		checkInflammation();
		
		//line to check and printcoordinates. 
		if (getTrackingvalue() == true)
		{
			if (this instanceof UncognateCell){
			
			GridPoint myPoint = grid.getLocation(this);
			double time = getTimeSinceEntered();
			double x = myPoint.getX();
			double y = myPoint.getY();
			double z = myPoint.getZ();
			ArrayList<Double>timelist = new ArrayList();
			timelist.add(time);
			timelist.add(x);
			timelist.add(y);
			timelist.add(z);
			
			//checking stimulation on cd 8 ( change line 77 to instanceof Cognate Cell
		//	boolean cd= ((CognateCell)this).getCD8();
		//	if (cd == true)
		//			{double t =8;timelist.add(t);};
		//	double s= ((CognateCell)this).getStimulation();
		//	timelist.add(s);		
			
			
			
			int is = getIstring();
			PrintTrack.printCoordinates(Constants.FILE_NAME,is,timelist);
			timelist.clear();
			}
		}
		
    	if (retainTime ==0){preStep();}
    	if (timeSinceEntered > 0){timeSinceEntered++;} // only updates newly entered cells
    	if (getTimeSinceEntered() == Constants.timePostEntryS1P1up) {updateInitalS1P1();}; // after 1 hours S1P1 upregulated. 
    	// but this contrast with inflammation? 
    	
    	SeeExit1();
    	
    	//need to only check age if cell hasn't exited so these two things need to be in the same code
    	//otherwise a null pointer could be returned
    	//checkAge();  	
    	
    }
    
    //I used this method in oct2020:density comment this out if you don't want this in then
   // @ScheduledMethod(start = 1, interval = 1 , priority = 9)
    public void StartUpdate2() throws IOException{
    	SeeExit3();
    }
    
    
    //I used this method in oct2020:hevcomment this out if you don't want this in then
     @ScheduledMethod(start = 1, interval = 1 , priority = 9)
     public void StartUpdate3() throws IOException{
     	SeeExit4();
     }
    
    
   // REMOVE THIS SECTION i think the version in the lymph node context thing worked better because it just 
    // checked tc's present rather than here you could actually have removed the TC elsewhere 
    //and be calling something else
   // this literally isn't working// maybe try doing it the other way. 
    // i think maybe the cells are retained with retention, 
  // @ScheduledMethod(start = 1, interval = 1 , priority = 9)
    public void ifCrowdedExit() throws IOException{
    	
   	 Context<Object> context = (Context)ContextUtils.getContext(this);
     GridValueLayer geometryLayer = (GridValueLayer)context.getValueLayer("Geometry");
     GridPoint pt = grid.getLocation(this);
     double value = geometryLayer.get(pt.getX(),pt.getY(),pt.getZ());
     
     if (value==18){
    	 if (getRetention()==0){
    		 GridCell<Tcell> cell = new GridCell<Tcell>(pt,Tcell.class);
    		 double size= cell.size();
    		if (size>1.0)
    		{System.out.println("Two t cells in the grid size: "+size);
    					seeIfExit();}
    	    
   }
     }
   }

   
  public void  updateInitalS1P1 ()
  {
	  //overwritten in subclasses as additional check for cognate cells that it's not activated and thus S1P1 regulated
  }
    
    
    
 public void SeeExit1() throws IOException //only cells that are not bound may exit
 {
	 
	 Context<Object> context = (Context)ContextUtils.getContext(this);
     GridValueLayer geometryLayer = (GridValueLayer)context.getValueLayer("Geometry");
     GridPoint pt = grid.getLocation(this);
     double value = geometryLayer.get(pt.getX(),pt.getY(),pt.getZ());
     if (value ==18)
     {
	 if (getRetention() ==0)
	 {
		
		 seeIfExit();
	 }
     }
     // this is to allow HEV exit with low probability but applying this here makes all the TCs leave
     
     //if (value ==24)
     //{
	 //if (getRetention() ==0)
	 //{
		// seeIfExit2();
	 //}
     //}
     
 }
 
 // called above :this one is a double check on the crowding - an extra round of exit possibility
 public void SeeExit3() throws IOException //only cells that are not bound may exit
 {
	 
	 Context<Object> context = (Context)ContextUtils.getContext(this);
     GridValueLayer geometryLayer = (GridValueLayer)context.getValueLayer("Geometry");
     GridPoint pt = grid.getLocation(this);
     double value = geometryLayer.get(pt.getX(),pt.getY(),pt.getZ());
     if (value ==18)
     {
	 if (getRetention() ==0)
	 {
		 GridCell<Tcell> cell = new GridCell<Tcell>(pt,Tcell.class);
		 double size= cell.size();
		 if (size>1){
			 seeIfExit2(); // so now you can vary Pe on this second thing
		 }
	 }
		 
	 }
     }
 
 
 // called above:this one is to allow TCs to exit through HEVs if there is two TCs per grid
 public void SeeExit4() throws IOException //only cells that are not bound may exit
 {
	 
	 Context<Object> context = (Context)ContextUtils.getContext(this);
     GridValueLayer geometryLayer = (GridValueLayer)context.getValueLayer("Geometry");
     GridPoint pt = grid.getLocation(this);
     double value = geometryLayer.get(pt.getX(),pt.getY(),pt.getZ());
     if (value ==24)
     {
	 if (getRetention() ==0)
	 {
		 GridCell<Tcell> cell = new GridCell<Tcell>(pt,Tcell.class);
		 double size= cell.size();
		 if (size>1){
			 seeIfExit2(); // so now you can vary Pe on this second thing
		 }
	 }
		 
	 }
     }
 
 
 
   
   public void seeIfExit() throws IOException
   {
	   //nothing, should be overwritten by subclass methods
   }
   
   
   public void seeIfExit2() throws IOException
   {
	   //nothing, should be overwritten by subclass methods
   }
   
   public void checkAge()
   {
	   //empty method to be overwritten in subclass
   }
 
 
  //adding this pre step in so that only cells that can actually move go through the step phase
    public void preStep()
    {   
    	//more computationally efficient to check beta first and skip the whole section if the cell doesn't move
    	Parameters params = RunEnvironment.getInstance().getParameters();
    	if (RandomHelper.nextIntFromTo(0,100) < ((Double)params.getValue("beta")))// where beta movement is % likely hood of moving
    	
    		{ 
    	   	
    		Context<Object> context = (Context)ContextUtils.getContext(this);
    		GridValueLayer geometryLayer = (GridValueLayer)context.getValueLayer("Geometry");
    		GridPoint pt = grid.getLocation(this);
    		double value = geometryLayer.get(pt.getX(),pt.getY(),pt.getZ());
        		//NdPoint pt2 = space.getLocation(this);
        		//double value2 = geometryLayer.get(pt2.getX(),pt2.getY(),pt2.getZ()); 	
        		//clean up code if the change in radiius is >1 comment out if testing 0.8 theory
        		// if (value2==0){StepIn(pt);}
        		// else {to set a percentage movement probability     		
    	   
    			if (value > 14 ) // extra else because don't want cell to do two moves
    			{
    				//add line here if option is one, step one, if option is two , step 2 etc. 
    				if ((int)params.getValue("Crowding_rules") ==1)
    					{step1(pt);}
    				else if ((int)params.getValue("Crowding_rules") ==2)
    					{step2(pt);}
    				
    				// then if you want to to check the cells again . 
    				//run step3?
    				  				
    				
    				else if ((int)params.getValue("Crowding_rules") ==3)
    					{step3(pt);}
    				else
    					{stepLeast(pt);}
    		   
    	   		} //if (value == 20){stepIn(pt);} //old method to prevent cells exiting too far
    	}
    //	else //don't move don't really need this else and it's more efficient without it. 
    	//	{};
    }
       
//movement method 1 > to a least crowded space

    public void stepLeast(GridPoint pt){
    
        Context<Object> context = (Context)ContextUtils.getContext(this);
        GridValueLayer geometryLayer = (GridValueLayer)context.getValueLayer("Geometry");
       List<GridCell<Tcell>> gridCells = getInsideGrids(pt);   
       //can remove grids with DCs too by using this // List<GridCell<Object>> gridCells= getInsideGridsDCs(pt);    
        SimUtilities.shuffle(gridCells, RandomHelper.getUniform());      
        GridPoint pointWithLeastTcells = null;
        int minCount = Integer.MAX_VALUE;                
        //find least Tcells    
        for (GridCell<Tcell>cell2:gridCells){
                if (cell2.size() < minCount) {
                    pointWithLeastTcells = cell2.getPoint();
                    minCount = cell2.size();
                    // i think there might be a bias here eg, probably checks the left bottom first
                }}
            moveTowards(pointWithLeastTcells);    
    }

     //no more than 1 per cell // note that up to june18 this was incorrect as it was 2 per cell, and step2, 3
    public void step1(GridPoint pt)
    {	Context<Object> context = (Context)ContextUtils.getContext(this);
       List<GridCell<Tcell>> gridCells = getInsideGrids(pt);      
       //then filter this    
       List<GridCell<Tcell>>gridList2 = new  ArrayList<GridCell<Tcell>>();
        for (GridCell<Tcell>cell2:gridCells){
                if (cell2.size() < 1) {
                	gridList2.add(cell2);}
        }
        if (gridList2.size()>=1){
        SimUtilities.shuffle(gridList2, RandomHelper.getUniform());  
           GridPoint point = gridList2.get(0).getPoint();   
            moveTowards(point);} 
        else
        {} // there is no where to move to // could add an exit thing here: seeifExit1()
        //COMPLETELY NO WHERE TO MOVE > COULD ADD IN AN EXIT THING
    	
    }
    //movement method 2 // to any random space , no more then 2
    public void step2(GridPoint pt) 
    {Context<Object> context = (Context)ContextUtils.getContext(this);
     List<GridCell<Tcell>> gridCells = getInsideGrids(pt);        
     List<GridCell<Tcell>>gridList2 = new  ArrayList<GridCell<Tcell>>();
     for (GridCell<Tcell>cell2:gridCells){
      if (cell2.size() < 2) {
       gridList2.add(cell2);}}
         if (gridList2.size()>=1){
         SimUtilities.shuffle(gridList2, RandomHelper.getUniform());  
            GridPoint point = gridList2.get(0).getPoint();   
             moveTowards(point);} 
         else {}}
    

    
    
    //no more than 3 per grid
    public void step3(GridPoint pt)
    {Context<Object> context = (Context)ContextUtils.getContext(this);
    List<GridCell<Tcell>> gridCells = getInsideGrids(pt);        
    List<GridCell<Tcell>>gridList2 = new  ArrayList<GridCell<Tcell>>();
    for (GridCell<Tcell>cell2:gridCells){
     if (cell2.size() < 3) {
      gridList2.add(cell2);}}
        if (gridList2.size()>=1){
        SimUtilities.shuffle(gridList2, RandomHelper.getUniform());  
           GridPoint point = gridList2.get(0).getPoint();   
            moveTowards(point);} 
        else {}// there is no where to move to
        }
    //could do with beta 60 and beta 70 > over 16 nodes = 200 cell tracks per speed
    
    public void stepD(GridPoint pt){
        
        Context<Object> context = (Context)ContextUtils.getContext(this);
        GridValueLayer geometryLayer = (GridValueLayer)context.getValueLayer("Geometry");
       List<GridCell<Object>> gridCells = getInsideGridsDCs(pt);   
       //can remove grids with DCs too by using this // List<GridCell<Object>> gridCells= getInsideGridsDCs(pt);    
        SimUtilities.shuffle(gridCells, RandomHelper.getUniform());      
        GridPoint pointWithLeastTcells = null;
        int minCount = Integer.MAX_VALUE;                
        //find least Tcells    
        for (GridCell<Object>cell2:gridCells){
                if (cell2.size() < minCount) {
                    pointWithLeastTcells = cell2.getPoint();
                    minCount = cell2.size();
                    // i think there might be a bias here eg, probably checks the left bottom first
                }}
            moveTowards(pointWithLeastTcells);    
    }
 //could also use a input and vary from the board
    //movement method 2 // to any random space , no more then 1 per cell
    public void stepD2(GridPoint pt)
    {	Context<Object> context = (Context)ContextUtils.getContext(this);
       List<GridCell<Object>> gridCells = getInsideGridsDCs(pt);      
       //then filter this    
       List<GridCell<Object>>gridList2 = new  ArrayList<GridCell<Object>>();
        for (GridCell<Object>cell2:gridCells){
                if (cell2.size() < 2) {
                	gridList2.add(cell2);}
        }
        if (gridList2.size()>=1){
        SimUtilities.shuffle(gridList2, RandomHelper.getUniform());  
           GridPoint point = gridList2.get(0).getPoint();   
            moveTowards(point);} 
        else
        {} // there is no where to move to
    	
    }
    //movement method 3 // to any random space , no more then 3 
    public void stepD3(GridPoint pt)
    {Context<Object> context = (Context)ContextUtils.getContext(this);
     List<GridCell<Object>> gridCells = getInsideGridsDCs(pt);        
     List<GridCell<Object>>gridList2 = new  ArrayList<GridCell<Object>>();
     for (GridCell<Object>cell2:gridCells){
      if (cell2.size() < 3) {
       gridList2.add(cell2);}}
         if (gridList2.size()>=1){
         SimUtilities.shuffle(gridList2, RandomHelper.getUniform());  
            GridPoint point = gridList2.get(0).getPoint();   
             moveTowards(point);} 
         else {} // there is no where to move to
    }
    //movement method 4 // to any random space, no more then 10 (basically allows loads
    public void stepD4(GridPoint pt)
    {Context<Object> context = (Context)ContextUtils.getContext(this);
    List<GridCell<Object>> gridCells = getInsideGridsDCs(pt);        
    List<GridCell<Object>>gridList2 = new  ArrayList<GridCell<Object>>();
    for (GridCell<Object>cell2:gridCells){
     if (cell2.size() < 4) {
      gridList2.add(cell2);}}
        if (gridList2.size()>=1){
        SimUtilities.shuffle(gridList2, RandomHelper.getUniform());  
           GridPoint point = gridList2.get(0).getPoint();   
            moveTowards(point);} 
        else {}// there is no where to move to
        }
    
   
    public void moveTowards(GridPoint pt){
        if (!pt.equals(grid.getLocation(this)))
        {
        	
        	GridPoint point = pt;
        	 grid.moveTo(this, pt.getX(),pt.getY(),pt.getZ());
        	
           //     NdPoint myPoint = space.getLocation(this);
             //   NdPoint otherPoint = new NdPoint(pt.getX(),pt.getY(),pt.getZ());
               //  double[] disp = space.getDisplacement(myPoint , otherPoint);
                //space.moveByDisplacement(this,disp);
                //myPoint = space.getLocation(this);
                //grid.moveTo(this, (int)myPoint.getX(),(int)myPoint.getY(),(int)myPoint.getZ());                    
        }}
        
public List<GridCell<Tcell>> getInsideGrids(GridPoint pt)
    {
        Context<Object> context = (Context)ContextUtils.getContext(this);
        GridValueLayer geometryLayer = (GridValueLayer)context.getValueLayer("Geometry");
        GridCellNgh<Tcell>nghCreator = new GridCellNgh<Tcell>(grid,pt,
                Tcell.class,1,1,1);
        List<GridCell<Tcell>>gridCells2 = nghCreator.getNeighborhood(true);
        
        //need to add a line of code here to filter out the points that are actually not inside the node
        List<GridCell<Tcell>>gridCells = new  ArrayList<GridCell<Tcell>>();
        for (GridCell<Tcell> cell : gridCells2){           
            GridPoint N = cell.getPoint();            
            if (geometryLayer.get(N.getX(),N.getY(),N.getZ()) > 15 ) // could alter this so it is ==20 too        
            {gridCells.add(cell);}
            //this is a method to get the neighbouring grids and filter out those that are not inside
        }
        
        return gridCells;
    }


    
    
 public List<GridCell<Object>>getInsideGridsDCs(GridPoint pt)
	{
		Context<Object> context = (Context)ContextUtils.getContext(this);
	    GridValueLayer geometryLayer = (GridValueLayer)context.getValueLayer("Geometry");
	    GridCellNgh<Object>nghCreator = new GridCellNgh<Object>(grid,pt,Object.class,1,1,1);
	    
	    List<GridCell<Object>>gridCells3 = nghCreator.getNeighborhood(true);
	    //this is the list of grid cells
	    
	    List<GridCell<Object>>gridCellsDC = new  ArrayList<GridCell<Object>>();
	    //this is the second list  of grid cells , ready to recieve gridcells in right geom
	    
	    //populate the list with the gridcells that are in the right geometry
	     for (GridCell<Object> cell : gridCells3)   
	    { GridPoint N = cell.getPoint(); 
	    if (geometryLayer.get(N.getX(),N.getY(),N.getZ()) > 15 ) // could alter this so it is ==20 too        
	        {gridCellsDC.add(cell);}}
	     
	    //third list ready to recieve only those gridcells with no DCs
	    List<GridCell<Object>>gridCellsDC2 = new  ArrayList<GridCell<Object>>();
	     
    //this is for every object in the grid cell not every grid cell, so if two cells present then it will run 18 times!
 //for (GridCell<Object> cell : gridCellsDC){
	  
	      int i = 0;
	    		  for (i = 0; i < gridCellsDC.size();i++){  //for i = 0-8
	    		        for (Object object : gridCellsDC.get(i).items())  //for Object object to gridcell[i].item()
	    		        {
	    		        	if (object instanceof DC){
	    		        		//if (!(object instanceof DCcell) || object == null){ // check that this returns the grids with the empty grids and T cellsgrids
	    		                gridCellsDC.remove(gridCellsDC.get(i)); }}}	  	
	    		  	return gridCellsDC;
		//this one is not adding the grid cells if they are empty, only if they have a T cells in 
	
	}
   

    public void setRetention(int retention)
    {
        this.retainTime = retention;
       
    }
    
    public int getRetention()
    {
        return retainTime;
    }
    
    public int getAge()
    {
        return age;
    }
    
    public void setAge(int age)
    {
        this.age = age;
    }
   
 
    public void setTimeSinceLastBound(int value)
    {
    	this.timeSinceLastBound = value;
    }

    public int getTimeSinceLastBound ()
    {
    	return timeSinceLastBound;
    }
    public void setTimeSinceEntered(int value)
    {
    	this.timeSinceEntered = value;
    }

    public int getTimeSinceEntered()
    {
    	return timeSinceEntered;
    }
    public void setDCContacted(int value)
    {
    	this.DCContacted = value;
    }

    public int getDCContacted()
    {
    	return DCContacted;
    }
    
    
    //clean up method (can comment out)
    public void StepIn(GridPoint pt){
    	// make a list of neighbors, if not ==1
    //	add to list, shuffle list and move
    	Context<Object> context = (Context)ContextUtils.getContext(this);
        GridValueLayer geometryLayer = (GridValueLayer)context.getValueLayer("Geometry");
    	GridCellNgh<Tcell>nghCreator = new GridCellNgh<Tcell>(grid,pt,
                 Tcell.class,2,2,2);
         List<GridCell<Tcell>>gridCells2 = nghCreator.getNeighborhood(true);
         //need to add a line of code here to filter out the points that are actually not inside the node
         List<GridCell<Tcell>>gridCells = new  ArrayList<GridCell<Tcell>>();
         for (GridCell<Tcell> cell : gridCells2){  
             GridPoint N = cell.getPoint();
             if (geometryLayer.get(N.getX(),N.getY(),N.getZ()) > 14 ) // could alter this so it is ==20 too        
             {gridCells.add(cell);}            
}   	
         SimUtilities.shuffle(gridCells, RandomHelper.getUniform());  
             GridPoint newPlace = gridCells.get(0).getPoint();
    	moveTowards(newPlace);
    }
    
  public void checkInflammation()
  {
	 //overwrite in subclass
	  
  }
	  
  
  public void setS1P1(double value)
  {
	 // this.S1P1 = value;
	  //overwrite in subclass
  }
  
  //public double getS1P1()
  //{
//	return S1P1;
 // }
    
  public int getIstring()
  {return istring;}
    public void setIstring(int istring)
    {
    	this.istring = istring;
    }
   public void setTracking(boolean value)
   {
	   this.trackingvalue = value;
	   }
   
   public boolean getTrackingvalue()
   {
	   return trackingvalue;
   }

}
  