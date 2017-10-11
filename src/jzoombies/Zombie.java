/**
 * 
 */
package jzoombies;

import java.util.ArrayList;
import java.util.List;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.graph.Network;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.SimUtilities;

/**
 * @author Muqtafi Akhmad
 *
 */
public class Zombie {
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	private boolean moved = false;
	
	public Zombie(ContinuousSpace<Object> space, Grid<Object> grid) {
		this.space = space;
		this.grid = grid;
		this.moved = false;
	}
	
	@ScheduledMethod(start = 1, interval = 1)
	public void step(){
		// get the grid location of this zombie
		GridPoint pt = grid.getLocation(this);
		
		// create grid cells for surrounding neighborhood
		GridCellNgh<Human> nghCreator = new GridCellNgh<Human>(this.grid, pt, Human.class, 1, 1);
		List<GridCell<Human>> gridCells = nghCreator.getNeighborhood(true);
		SimUtilities.shuffle(gridCells, RandomHelper.getUniform());
		
		GridPoint pointWithMostHumans = null;
		int maxCount = -1;
		for(GridCell<Human> cell : gridCells){
			if(cell.size() > maxCount){
				pointWithMostHumans = cell.getPoint();
				maxCount = cell.size();
			}
		}
		moveTowards(pointWithMostHumans);
		infect();
	}
	
	public void moveTowards(GridPoint pt){
		// only move if zombie is not in this location
		if(!pt.equals(this.grid.getLocation(this))){
			NdPoint myPoint = space.getLocation(this);
			NdPoint otherPoint = new NdPoint(pt.getX(), pt.getY());
			double angle = SpatialMath.calcAngleFor2DMovement(space, myPoint, otherPoint);
			space.moveByVector(this, 1, angle, 0);
			myPoint = space.getLocation(this);
			grid.moveTo(this, (int) myPoint.getX(), (int) myPoint.getY());
			
			this.moved = true;
		}else{
			this.moved = false;
		}
	}
	
	public void infect(){
		GridPoint pt = this.grid.getLocation(this);
		List<Object> humans = new ArrayList<Object>();
		// find human in zombie location
		for(Object object : grid.getObjectsAt(pt.getX(), pt.getY())){
			if(object instanceof Human){
				humans.add(object);
			}
		}
		// if there is any human, infect one human randomly (change them with zombies)
		if(humans.size() > 0){
			int index = RandomHelper.nextIntFromTo(0, humans.size() - 1);
			Object obj = humans.get(index);
			NdPoint spacePt = space.getLocation(obj);
			Context<Object> context = ContextUtils.getContext(obj);
			// remove human
			context.remove(obj);
			// create and add zombie
			Zombie zombie = new Zombie(space, grid);
			context.add(zombie);
			space.moveTo(zombie, spacePt.getX(), spacePt.getY());
			grid.moveTo(zombie, pt.getX(), pt.getY());
			Network<Object> net = (Network<Object>) context.getProjection("infection network");
			net.addEdge(this, zombie);
		}
	}
}
