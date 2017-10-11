package jzoombies;

import java.util.List;

import repast.simphony.engine.watcher.Watch;
import repast.simphony.engine.watcher.WatcherTriggerSchedule;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.SimUtilities;

public class Human {
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	private int energy, startingEnergy;
	public Human(ContinuousSpace<Object> space, Grid<Object> grid, int energy) {
		this.space = space;
		this.grid = grid;
		this.energy = this.startingEnergy = energy;
	}
	@Watch(watcheeClassName = "jzoombies.Zombie",
			watcheeFieldNames = "moved",
			query="within_moore 1",
			whenToTrigger=WatcherTriggerSchedule.IMMEDIATE)
	public void run(){
		// get the grid location of this human
		GridPoint pt = this.grid.getLocation(this);
		// create grid cells for surrounding neighbor
		GridCellNgh<Zombie> nghCreator = new GridCellNgh<Zombie>(grid, pt, Zombie.class, 1, 1);
		List<GridCell<Zombie>> gridCells = nghCreator.getNeighborhood(true);
		SimUtilities.shuffle(gridCells, RandomHelper.getUniform());
		
		GridPoint pointWithLeastZombies = null;
		int minCount = Integer.MAX_VALUE;
		for(GridCell<Zombie> cell : gridCells){
			if(cell.size() < minCount){
				pointWithLeastZombies = cell.getPoint();
				minCount = cell.size();
			}
		}
		
		if(this.energy > 0){
			moveTowards(pointWithLeastZombies);
		}else{
			// recharge energy
			this.energy = this.startingEnergy;
		}
	}
	
	public void moveTowards(GridPoint pt){
		if(!pt.equals(grid.getLocation(this))){
			NdPoint myPoint = this.space.getLocation(this);
			NdPoint otherPoint = new NdPoint(pt.getX(), pt.getY());
			double angle = SpatialMath.calcAngleFor2DMovement(this.space, myPoint, otherPoint);
			
			this.space.moveByVector(this, 2, angle, 0);
			myPoint = this.space.getLocation(this);
			this.grid.moveTo(this, (int) myPoint.getX(), (int) myPoint.getY());
			this.energy--;
		}
	}
}
