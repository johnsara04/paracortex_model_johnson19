package lN_simple5;

import java.awt.Color;

import javax.media.j3d.Shape3D;

import repast.simphony.visualization.visualization3D.AppearanceFactory;
import repast.simphony.visualization.visualization3D.ShapeFactory;
import repast.simphony.visualization.visualization3D.style.DefaultStyle3D;
import repast.simphony.visualization.visualization3D.style.TaggedAppearance;
import repast.simphony.visualization.visualization3D.style.TaggedBranchGroup;

public class CellStyle2 extends DefaultStyle3D<Tcell> {

	//from physics shapes
	
	
	
	
	
	public TaggedBranchGroup getBranchGroup(Tcell agent, TaggedBranchGroup taggedGroup) {
		if (taggedGroup == null) { taggedGroup = new TaggedBranchGroup("DEFAULT");
			Shape3D sphere = ShapeFactory.createSphere(.02f, "DEFAULT");// createCube(.03f, "DEFAULT");
			taggedGroup.getBranchGroup().addChild(sphere);
			return taggedGroup;
		}

		return null;
	}

	public TaggedAppearance getAppearance(Tcell agent, TaggedAppearance taggedAppearance,
			Object shapeID) {
		if (taggedAppearance == null) {
			taggedAppearance = new TaggedAppearance("DEFAULT");
			AppearanceFactory.setMaterialAppearance(taggedAppearance.getAppearance(), Color.BLUE);}
			
			
		
		// can then differentiate between cell appearance 
			int timesinceentered = agent.getTimeSinceEntered();
			
			if  (timesinceentered < 50)
				{AppearanceFactory.setMaterialAppearance(taggedAppearance.getAppearance(), Color.RED); }
			else if (timesinceentered >= 50)   
				{ 
				AppearanceFactory.setMaterialAppearance(taggedAppearance.getAppearance(), Color.BLUE);} 
				// can also define the color of the agent in 
				//the cell file it'self under check age then do agent.getColor. Does mean each agent has to store a colour variable too though, 
				//when the info is really already stored in it's age. 
			
			if (agent instanceof CognateCell )
					{   if  (((CognateCell)agent).getActivation() == true)	
					{  AppearanceFactory.setMaterialAppearance(taggedAppearance.getAppearance(), Color.YELLOW);}
					{   if  (((CognateCell)agent).getEffector() == true)	
						{AppearanceFactory.setMaterialAppearance(taggedAppearance.getAppearance(), Color.GREEN);}
					}
					{   if  (((CognateCell)agent).getM() == true)	
					{AppearanceFactory.setMaterialAppearance(taggedAppearance.getAppearance(), Color.CYAN);}
				}
					
					}	
					
					
			
			
			
		
		
		return taggedAppearance;
	}
	
	
	
	
	
}
