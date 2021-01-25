package lN_simple5;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.measure.quantity.Length;

import repast.simphony.context.Context;
//import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
//import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.continuous.SimpleCartesianAdder;
import repast.simphony.space.gis.SimpleAdder;
import repast.simphony.space.grid.BouncyBorders;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.WrapAroundBorders;
import repast.simphony.util.SimUtilities;
import repast.simphony.valueLayer.GridValueLayer;


public class lymph_nodeBuilder implements ContextBuilder<Object> {

//first make the context 

@Override 

public Context build (Context<Object>context){
	

	NetworkBuilder<Object>netBuilder = new NetworkBuilder<Object>("DC interaction network", context,true);
	netBuilder.buildNetwork();
	GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
	Grid<Object> grid = gridFactory.createGrid("grid", context,
	new GridBuilderParameters<Object>(new BouncyBorders(),   //new WrapAroundBorders()
	new SimpleGridAdder<Object>(),
	true,Constants.GRID_WIDTH,Constants.GRID_SIZE,Constants.GRID_SIZE));

	//set up parameters
	Parameters params = RunEnvironment.getInstance().getParameters();
	double Inner_Radius = Constants.INNER_RADIUS;
	double Outer_Radius = Constants.OUTER_RADIUS;
	double entry_percent = Constants.ENTRY_PERCENTAGE;
	double exit_percent = Constants.EXIT_PERCENTAGE;  
	int gridWidth = Constants.GRID_WIDTH;
	int gridHeight = Constants.GRID_SIZE;
	int gridz = Constants.GRID_SIZE;
	int TCellCount = Constants.Initial_Tcellcount;
	int DCCount = Constants.initialDC;//(int)params.getValue("initialDC");//Constants.INITIAL_DC_COUNT; //(Integer)params.getValue("DC_count");
	int centreP = Constants.CENTRE_POINT; //(for a hemisphere this is only for j and k , for a quarter this is only for j else is 0
	int centreX= 0;

	//Duration of simulation
	RunEnvironment.getInstance().endAt(Constants.SIMULATION_LENGTH);

	//and set up geometry value layer

	GridValueLayer geometryLayer = new GridValueLayer("Geometry",true,
			new BouncyBorders(), Constants.GRID_WIDTH,Constants.GRID_SIZE,Constants.GRID_SIZE); //new WrapAroundBorders()
			for (int i=0; i<gridWidth; i++){
				for (int j=0; j<gridHeight; j++){
					for (int k = 0; k < gridz; k++){
						geometryLayer.set(0,i, j,k);
						context.addValueLayer(geometryLayer);
					}}}

			// extra value layer to take a cross section of the GridValueLayer geometry
			GridValueLayer crossSectionLayer = new GridValueLayer("CrossSection",true,
					new BouncyBorders(), Constants.GRID_SIZE,Constants.GRID_SIZE); //new WrapAroundBorders()
						for (int j=0; j<gridHeight; j++){
							for (int k = 0; k < gridz; k++){
								crossSectionLayer.set(0,j,k);
								context.addValueLayer(crossSectionLayer);
							}}
			//also need a relevant 2D grid for that 
						GridFactory gridFactory2 = GridFactoryFinder.createGridFactory(null);
						Grid<Object> grid2 = gridFactory.createGrid("grid2", context,
						new GridBuilderParameters<Object>(new BouncyBorders(),   //new WrapAroundBorders()
						new SimpleGridAdder<Object>(),true,Constants.GRID_SIZE,Constants.GRID_SIZE));


			//make boundary
						for (int i =0; i < gridWidth; i++){
							for (int j = 0; j < gridHeight; j++){
								for (int k = 0; k < gridz; k++){
									GridPoint centre = new GridPoint(centreX,centreP,centreP);
									GridPoint otherPoint= new GridPoint(i,j,k);
									if (grid.getDistance(otherPoint,centre)  < Outer_Radius )
									{geometryLayer.set(15,i, j,k);}}}}

						double entry_radius = entry_percent * Outer_Radius;
						double exit_radius = exit_percent * Outer_Radius;
						double exit_height = 0.75 * Outer_Radius + (gridHeight/2); //75% of the height of the node

			//make general inside
						for (int i =0; i < gridWidth; i++){
							for (int j = 0; j < gridHeight; j++){
								for (int k = 0; k < gridz; k++){
									GridPoint centre = new GridPoint(centreX,centreP,centreP);
									GridPoint otherPoint= new GridPoint(i,j,k);
									if (grid.getDistance(otherPoint,centre)  < Inner_Radius )
									{geometryLayer.set(20,i, j,k);}
								}}}
			//make exit 
						for (int i =0; i < gridWidth; i++){
							for (int j = 0; j < gridHeight; j++){
								for (int k = 0; k < gridz; k++){
									GridPoint centre = new GridPoint(centreX,centreP,centreP);
									GridPoint otherPoint= new GridPoint(i,j,k);
									if (j < exit_height ){   
										if (grid.getDistance(otherPoint,centre)  < Inner_Radius )
										{geometryLayer.set(18,i, j,k);}} }}}

			//make rest of entry
						for (int i =0; i < gridWidth; i++){
							for (int j = 0; j < gridHeight; j++){
								for (int k = 0; k < gridz; k++){
									GridPoint centre = new GridPoint(centreX,centreP,centreP);
									GridPoint otherPoint= new GridPoint(i,j,k);
									if (grid.getDistance(otherPoint,centre)  < exit_radius )
									{geometryLayer.set(20,i, j,k);}}}}

			//make inside
						for (int i =0; i < gridWidth; i++){
							for (int j = 0; j < gridHeight; j++){
								for (int k = 0; k < gridz; k++){
									GridPoint centre = new GridPoint(centreX,centreP,centreP);
									GridPoint otherPoint= new GridPoint(i,j,k);
									if (grid.getDistance(otherPoint,centre) < entry_radius)
									{geometryLayer.set(24,i, j,k);}
								}}}

			// take the middle cross section and convert to a 2 D grid
						for (int j=0; j<gridHeight; j++){
							for (int k = 0; k < gridz; k++){
								double v1 = geometryLayer.get(1,j,k);
								crossSectionLayer.set(v1,j,k);}}

			//then add create and objects to the context
			System.out.println("Initial number of T cell = "+ Constants.Initial_Tcellcount);
			int numberofcognate = (int)Math.round(Constants.Initial_Tcellcount *(Double)params.getValue("cognition"));// (Constants.COGNATE_VALUE ));
			int numberCD4 = (int)Math.round(Constants.RatioCD4*numberofcognate);
			System.out.println("Number of CD4 "+numberCD4);
			int numberCD8 = (int)Math.round(Constants.RatioCD8*numberofcognate);
			System.out.println("Number of CD8 "+numberCD8);
			int numberNormalCells = TCellCount - numberofcognate;
			int age = RandomHelper.createNormal(1080,100).nextInt();
			int retain_time = 0;
			int timeSinceFirstBound =0;
			int timeSinceLastBound = 11;
			boolean activated = false;
			boolean effector = false;
			double stimulation = 0;
			int profCount = 0;
			double S1P1 = 1; 
			int istring = 0;
			boolean trackingvalue = false;
			int timeAct = 0;
			int timeSinceEntered = 0;
			int DCContacted = 0;
			int TimeSinceDif = 0;
			boolean M=false;
			for (int i =0;i<numberNormalCells; i++) {
			context.add(new UncognateCell(
			grid,	
			retain_time,
			age,
			timeSinceLastBound,
			timeSinceEntered,
			DCContacted,
			S1P1,
			istring,
			trackingvalue
		));}
// need to alter the random adder so that it only adds to grid points with centre point 

for (int i = 0; i <numberCD4;i++){
	boolean CD4=true;
	boolean CD8 = false;
	context.add(new CognateCell(
			grid, 		
			retain_time,
			age,
	        timeSinceLastBound,
	        timeSinceEntered,
	        DCContacted,
	        timeSinceFirstBound,
	        stimulation,
	        activated,
	        effector,
	        profCount,
	        S1P1,
	        istring,
	        trackingvalue,
	        timeAct,
	        CD4,
	        CD8,
	        M,
	        TimeSinceDif
	        ));
	
}

for (int i = 0; i <numberCD8;i++){
	boolean CD4=false;
	boolean CD8 = true;
	context.add(new CognateCell(
			grid, 		
			retain_time,
			age,
	        timeSinceLastBound,
	        timeSinceEntered,
	        DCContacted,
	        timeSinceFirstBound,
	        stimulation,
	        activated,
	        effector,
	        profCount,
	        S1P1,
	        istring,
	        trackingvalue,
	        timeAct,
	        CD4,
	        CD8,
	        M,
	        TimeSinceDif
	        ));
	
}

//option to add all the DCs here if wanted,
for (int i = 0;i<DCCount;i++){
    int retainTime = 0;
    int boundCount = 0;
    double value =(Double)params.getValue("MHCstartingvalue");
    double MHCI  = RandomHelper.createNormal(value, 10).nextDouble();//RandomHelper.nextIntFromTo(200, 280);
    double MHCII  = RandomHelper.createNormal(value, 10).nextDouble();
    int TcellsContacted = 0;
    int cogTcellsContacted=0;
    int age2 = 1;
    boolean licenced = false;
    context.add(new DC(
    		grid,
    		retainTime,
    		MHCI,
    		MHCII,
    		boundCount,
    		TcellsContacted,
    		cogTcellsContacted,
    		age2,
    		licenced
    		));
    //context add ensures they are added to the collection context.
    }

	//list the areas the objects can be added to   
	List<GridPoint>FreePoints = new ArrayList<GridPoint>();
	for (int r =0; r < gridWidth; r++)
		{ for (int j = 0; j < gridHeight; j++)
    		{for (int k = 0; k < gridz; k++)
    			{ GridPoint centre = new GridPoint(centreX,centreP,centreP);
    			GridPoint otherPoint= new GridPoint(r,j,k);
    			if (grid.getDistance(otherPoint,centre) < (Constants.INNER_RADIUS - 1))
    				{FreePoints.add(otherPoint);}}}}
	
	//move objects to points in the list
	for (Object obj:context)
		{ SimUtilities.shuffle(FreePoints, RandomHelper.getUniform());    
			int index = 0;
			GridPoint point = FreePoints.get(index);	
			grid.moveTo (obj, (int)point.getX(),(int)point.getY(),(int)point.getZ());}

	//if want to count Cells added,
		int index3 =0;
		int index4 = 0;
		for (Object obj:context){
			if (obj instanceof CognateCell)
			{
				if ( ((CognateCell)obj).getCD4() == true)
				{index3++;}
				if (((CognateCell)obj).getCD8() == true)
				{index4++;}	}}
		System.out.println("CD4:CD8 " + index3 +" "+index4);
		
return context;

}

}
