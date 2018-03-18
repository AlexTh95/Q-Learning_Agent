import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;

import actor.*;
import grid.*;


public class QLearningBug extends Bug{
	
	private double learning_rate;
	private double epsilon;
	private double discount_factor;
	
	private double[][] R;
	private double[][] q_table;
	
	private ActorWorld world;
	private int rows;
	private int columns;
	private Location init_loc;
	private Location curr_loc;
	private Location target_loc;
	
	private int exploration_counter;
	private boolean target_found;
	
	private double penalty = -0.1;
	private double reward = 10;
	private double neg_reward = -10;
	
	private int turns;
	
	
	public QLearningBug(ActorWorld w,int start_row, int start_col) {
		super();
		// TODO Auto-generated constructor stub
		//putSelfInGrid(w.getGrid(),new Location(start_row,start_col));
		learning_rate = 0.8;
		discount_factor = 0.8;
		
		world = w;
		rows = world.getGrid().getNumRows();
		columns = world.getGrid().getNumCols();
		
		init_loc = new Location(start_row, start_col);
		curr_loc = new Location(start_row, start_col);
		
		q_table = new double[rows*columns][rows*columns];
		R = new double[rows*columns][rows*columns];
		
		turns=0;
		exploration_counter=0;
		target_found=true;
		
		initR();
				
	}
	
	void initR(){
		// We will navigate through the reward matrix R using k index
        for (int k = 0; k < rows*columns; k++) {

            // We will navigate with i and j through the maze, so we need
            // to translate k into i and j
            int i = k / rows;
            int j = k - i * rows;

            // Fill in the reward matrix with -1
            for (int s = 0; s < rows*columns; s++) {
                R[k][s] = -1;
            }
            
            //try moving in all directions in the maze
            Grid<Actor> gr = world.getGrid();
            if(gr==null){
            	return;
            }

                // Try to move left in the maze
                int goLeft = j - 1;
                if (goLeft >= 0) {
                    int target = i * rows + goLeft;
                    if (gr.get(new Location(i,goLeft)) == null) {
                        R[k][target] = 0;
                    } else if (gr.get(new Location(i,goLeft)) instanceof Flower) {
                        R[k][target] = reward;
                        target_loc = new Location(target/rows, target%rows);
                    }
                    else if (gr.get(new Location(i,goLeft)) instanceof Critter) {
                        R[k][target] = neg_reward;
                    }
                    else {
                    	R[k][target] = 0;
                    }
                }

                // Try to move right in the maze
                int goRight = j + 1;
                if (goRight < rows) {
                    int target = i * rows + goRight;
                    if (gr.get(new Location(i,goRight)) == null) {
                        R[k][target] = 0;
                    } else if (gr.get(new Location(i,goRight)) instanceof Flower) {
                        R[k][target] = reward;
                    } else if (gr.get(new Location(i,goRight)) instanceof Critter) {
                        R[k][target] = neg_reward;
                    } 
                    else {
                    	R[k][target] = 0;
                    }
                }

                // Try to move up in the maze
                int goUp = i - 1;
                if (goUp >= 0) {
                    int target = goUp * rows + j;
                    if (gr.get(new Location(goUp,j)) == null) {
                        R[k][target] = 0;
                    } else if (gr.get(new Location(goUp,j)) instanceof Flower) {
                        R[k][target] = reward;
                    } else if (gr.get(new Location(goUp,j)) instanceof Critter) {
                        R[k][target] = neg_reward;
                    } 
                    else {
                    	R[k][target] = 0;
                    }
                }

                // Try to move down in the maze
                int goDown = i + 1;
                if (goDown < columns) {
                    int target = goDown * rows + j;
                    if (gr.get(new Location(goDown,j)) == null) {
                        R[k][target] = 0;
                    } else if (gr.get(new Location(goDown,j)) instanceof Flower) {
                        R[k][target] = reward;
                    } else if (gr.get(new Location(goDown,j)) instanceof Critter) {
                        R[k][target] = neg_reward;
                    } 
                    else {
                    	R[k][target] = 0;
                    }
            }
            
        }
        
        //printR(R);
		
	}
	
	void printR(double[][] matrix) {

        for (int i = 0; i < rows*columns; i++) {
            System.out.print("Possible states from " + i + " :[");
            for (int j = 0; j < rows*columns; j++) {
                System.out.printf("%4s", matrix[i][j]);
            }
            System.out.println("]");
        }
    }

	@Override
	public void act() {
		
		//if the target moves the agent will know
		if (world.getGrid().get(target_loc)==null) {
			target_found=false;
			//find new target location
			newTargLoc();
		}
		if(!isTarget_found()) exploration_counter++;
		
		//if target can't be found and agent is "near" the target "area" gradually increase exploration rate 
		if(!isTarget_found() && getExploration_counter()>40){
			if(epsilon<0.8){
				epsilon+=((double) getExploration_counter())/100000;
			}else {
				//if target yet to be found decrease epsilon again so the agent returns at the target "area"
				epsilon = 0.2;
			}
		}
		else{
			epsilon = 0.2;
		}
		
			
		
        	Location loc = curr_loc;
            int crtState = loc.getRow()*rows + loc.getCol();
            
            //find possible moves
            double q_up = -10000;
            double q_down = -10000;
            double q_left = -10000;
            double q_right = -10000;
            
            int nextState_up = -1;
            int nextState_down = -1;
            int nextState_left = -1;
            int nextState_right = -1;
            
            Random rand = new Random();
            double  n = rand.nextDouble();
            
            if (n>epsilon){
            
            	if (world.getGrid().isValid(new Location (loc.getRow()-1,loc.getCol()))){
            		Location loc_up = new Location (loc.getRow()-1,loc.getCol());
            		nextState_up = loc_up.getRow()*rows + loc_up.getCol();
            		q_up = q_table[crtState][nextState_up];
            	}
            	if (world.getGrid().isValid(new Location (loc.getRow()+1,loc.getCol()))){
            		Location loc_down = new Location (loc.getRow()+1,loc.getCol());
            		nextState_down = loc_down.getRow()*rows + loc_down.getCol();
            		q_down = q_table[crtState][nextState_down];
            	}
            	if (world.getGrid().isValid(new Location (loc.getRow(),loc.getCol()-1))){
            		Location loc_left = new Location (loc.getRow(),loc.getCol()-1);
            		nextState_left = loc_left.getRow()*rows + loc_left.getCol();
            		q_left = q_table[crtState][nextState_left];
            	}
            	if (world.getGrid().isValid(new Location (loc.getRow(),loc.getCol()+1))){
            		Location loc_right = new Location (loc.getRow(),loc.getCol()+1);
            		nextState_right = loc_right.getRow()*rows + loc_right.getCol();
            		q_right = q_table[crtState][nextState_right];
            	}
            
            	//make move and update q_table
            	if (q_up > q_down && q_up > q_left && q_up > q_right){
            		setDirection(0);
            	
            		// Q(state,action)= Q(state,action) + alpha * (R(state,action) + gamma * Max(next state, all actions) - Q(state,action))
            		double q = q_table[crtState][nextState_up];
            		double maxQ = maxQ(nextState_up);
            		double r = R[crtState][nextState_up] + penalty;

            		q_table[crtState][nextState_up] = q + learning_rate * (r + discount_factor * maxQ - q);
                
            		//if flower or critter is at next location
            		if (R[crtState][nextState_up]==reward){
            			moveTo(init_loc);
            			curr_loc = init_loc;
            			target_found=true;
            			exploration_counter=0;
            		}
            		else if (R[crtState][nextState_up]==neg_reward){
            			moveTo(init_loc);
            			curr_loc = init_loc;
            		}
            		else if (canMove()){
            			move();
                		curr_loc = new Location(loc.getRow()-1,loc.getCol());
            		}
            		else {
            			q_table[crtState][nextState_up]=-10; //if there is a rock, no choose this action again
            		}
            	}
            	else if (q_down > q_up && q_down > q_left && q_down > q_right){
            		setDirection(180);
            	
            		// Q(state,action)= Q(state,action) + alpha * (R(state,action) + gamma * Max(next state, all actions) - Q(state,action))
            		double q = q_table[crtState][nextState_down];
            		double maxQ = maxQ(nextState_down);
            		double r = R[crtState][nextState_down] + penalty;

            		q_table[crtState][nextState_down] = q + learning_rate * (r + discount_factor * maxQ - q);
                
            		if (R[crtState][nextState_down]==reward){
            			moveTo(init_loc);
            			curr_loc = init_loc;
            			target_found=true;
            			exploration_counter=0;
            		}
            		else if (R[crtState][nextState_down]==neg_reward){
            			moveTo(init_loc);
            			curr_loc = init_loc;
            		}
            		else if (canMove()){
            			move();
            			curr_loc = new Location(loc.getRow()+1,loc.getCol());
            		}
            		else {
            			q_table[crtState][nextState_down]=-10;
            		}
                }
            	else if (q_right > q_down && q_right > q_left && q_right > q_up){
            		setDirection(90);
            	
            		// Q(state,action)= Q(state,action) + alpha * (R(state,action) + gamma * Max(next state, all actions) - Q(state,action))
            		double q = q_table[crtState][nextState_right];
            		double maxQ = maxQ(nextState_right);
            		double r = R[crtState][nextState_right] + penalty;

            		q_table[crtState][nextState_right] = q + learning_rate * (r + discount_factor * maxQ - q);
                
            		if (R[crtState][nextState_right]==reward){
            			moveTo(init_loc);
            			curr_loc = init_loc;
            			target_found=true;
            			exploration_counter=0;
            		}
            		else if (R[crtState][nextState_right]==neg_reward){
            			moveTo(init_loc);
            			curr_loc = init_loc;
            		}
            		else if (canMove()){
            			move();
            			curr_loc = new Location(loc.getRow(),loc.getCol()+1);
            		}
            		else {
            			q_table[crtState][nextState_right]=-10;
            		}
            		
                }
            	else if (q_left > q_down && q_left > q_right && q_left > q_up){
            		setDirection(270);
            	
            		// Q(state,action)= Q(state,action) + alpha * (R(state,action) + gamma * Max(next state, all actions) - Q(state,action))
            		double q = q_table[crtState][nextState_left];
            		double maxQ = maxQ(nextState_left);
            		double r = R[crtState][nextState_left] + penalty;

            		q_table[crtState][nextState_left] = q + learning_rate * (r + discount_factor * maxQ - q);
                
            		if (R[crtState][nextState_left]==reward){
            			moveTo(init_loc);
            			curr_loc = init_loc;
            			target_found=true;
            			exploration_counter=0;
            		}
            		else if (R[crtState][nextState_left]==neg_reward){
            			moveTo(init_loc);
            			curr_loc = init_loc;
            		}
	                else if (canMove()){
	                	move();
	                	curr_loc = new Location(loc.getRow(),loc.getCol()-1);
	                }
	                else {
            			q_table[crtState][nextState_left]=-10;
            		}
	            }
            	else {
            		makeRandomMove(crtState); //if no belief is the best then make random move
            	}
            }  
            else{
            	makeRandomMove(crtState); 
            }
            
            turns++;
    		System.out.println(epsilon);
    		System.out.println(turns);



        }
	
	public void newTargLoc(){
		
		//remove target from R table
		int target = target_loc.getRow()*rows + target_loc.getCol();
		
		if(target-rows>=0)           R[target-rows][target] = 0;
		if(target+rows<rows*columns) R[target+rows][target] = 0;
		if(target%rows<columns-1)      R[target+1][target] = 0;
		if(target%rows>0)            R[target-1][target] = 0;
		
		//find new location of target
		Location loc = new Location(target_loc.getRow()+1,target_loc.getCol());
		if(world.getGrid().isValid(loc)){
			if(world.getGrid().get(loc) instanceof Flower){
				target_loc = loc;
			}
		}
		loc = new Location(target_loc.getRow()-1,target_loc.getCol());
		if(world.getGrid().isValid(loc)){
			if(world.getGrid().get(loc) instanceof Flower){
				target_loc = loc;
			}
		}
		loc = new Location(target_loc.getRow(),target_loc.getCol()+1);
		if(world.getGrid().isValid(loc)){
			if(world.getGrid().get(loc) instanceof Flower){
				target_loc = loc;
			}
		}
		loc = new Location(target_loc.getRow(),target_loc.getCol()-1);
		if(world.getGrid().isValid(loc)){
			if(world.getGrid().get(loc) instanceof Flower){
				target_loc = loc;
			}
		}
		
		
		//fix R table according to target's new location
		target = target_loc.getRow()*rows + target_loc.getCol();
		if(target-rows>=0)           R[target-rows][target] = reward;
		if(target+rows<rows*columns) R[target+rows][target] = reward;
		if(target%rows<columns-1)      R[target+1][target] = reward;
		if(target%rows>0)            R[target-1][target] = reward;
	}
	
	public void makeRandomMove(int crtState){
	
		Random rand = new Random();
		int k = rand.nextInt(4);
		
		if(k==0 && crtState<rows)         k=1; //if unable to move up go down
		if(k==1 && crtState/rows>=rows-1) k=0; //if unable to move down go up
		if(k==2 && crtState%rows==9)      k=3; //if unable to move right go left
		if(k==3 && crtState%rows==0)      k=2; //if unable to move left go right
    	
    	if(k==0){
    		setDirection(0);
        	
    		// Q(state,action)= Q(state,action) + alpha * (R(state,action) + gamma * Max(next state, all actions) - Q(state,action))
    		double q = q_table[crtState][crtState-rows];
    		double maxQ = maxQ(crtState-rows);
    		double r = R[crtState][crtState-rows] + penalty;

    		q_table[crtState][crtState-rows] = q + learning_rate * (r + discount_factor * maxQ - q);
        
    		if (R[crtState][crtState-rows]==reward || R[crtState][crtState-rows]==neg_reward){
    			moveTo(init_loc);
    			curr_loc = init_loc;
    		}
    		else if (canMove()){
    			move();
        		curr_loc = new Location(curr_loc.getRow()-1,curr_loc.getCol());
    		}
    		else {
    			q_table[crtState][crtState-rows]=-10;
    		}
    	}
    	else if(k==1){
    		setDirection(180);
        	
    		// Q(state,action)= Q(state,action) + alpha * (R(state,action) + gamma * Max(next state, all actions) - Q(state,action))
    		double q = q_table[crtState][crtState+rows];
    		double maxQ = maxQ(crtState+rows);
    		double r = R[crtState][crtState+rows] + penalty;

    		q_table[crtState][crtState+rows] = q + learning_rate * (r + discount_factor * maxQ - q);
        
    		if (R[crtState][crtState+rows]==reward || R[crtState][crtState+rows]==neg_reward){
    			moveTo(init_loc);
    			curr_loc = init_loc;
    		}
    		else if (canMove()){
    			move();
    			curr_loc = new Location(curr_loc.getRow()+1,curr_loc.getCol());
    		}
    		else {
    			q_table[crtState][crtState+rows]=-10;
    		}
    	}
    	else if(k==2){
    		setDirection(90);
        	
    		// Q(state,action)= Q(state,action) + alpha * (R(state,action) + gamma * Max(next state, all actions) - Q(state,action))
    		double q = q_table[crtState][crtState+1];
    		double maxQ = maxQ(crtState+1);
    		double r = R[crtState][crtState+1] + penalty;

    		q_table[crtState][crtState+1] = q + learning_rate * (r + discount_factor * maxQ - q);
        
    		if (R[crtState][crtState+1]==reward || R[crtState][crtState+1]==neg_reward){
    			moveTo(init_loc);
    			curr_loc = init_loc;
    		}
    		else if (canMove()){
    			move();
    			curr_loc = new Location(curr_loc.getRow(),curr_loc.getCol()+1);
    		}
    		else {
    			q_table[crtState][crtState+1]=-10;
    		}
    	}
    	else if(k==3){
    		setDirection(270);
    		
    		
    		// Q(state,action)= Q(state,action) + alpha * (R(state,action) + gamma * Max(next state, all actions) - Q(state,action))
    		double q = q_table[crtState][crtState-1];
    		double maxQ = maxQ(crtState-1);
    		double r = R[crtState][crtState-1] + penalty;

    		q_table[crtState][crtState-1] = q + learning_rate * (r + discount_factor * maxQ - q);
        
    		if (R[crtState][crtState-1]==reward || R[crtState][crtState-1]==neg_reward){
            	moveTo(init_loc);
            	curr_loc = init_loc;
            }
            else if (canMove()){
            	move();
            	curr_loc = new Location(curr_loc.getRow(),curr_loc.getCol()-1);
            }
            else {
    			q_table[crtState][crtState-1]=-10;
    		}
    		
    	}
		
	}
			
	double maxQ(int nextState) {
        int[] actionsFromState = possibleActionsFromState(nextState);
        double maxValue = Double.MIN_VALUE;
        for (int nextAction : actionsFromState) {
            double value = q_table[nextState][nextAction];

            if (value > maxValue)
                maxValue = value;
        }
        return maxValue;
    }
	
	int[] possibleActionsFromState(int state) {
        ArrayList<Integer> result = new ArrayList<>();
        for (int i = 0; i < rows*columns; i++) {
            if (R[state][i] != -1) {
                result.add(i);
            }
        }

        return result.stream().mapToInt(i -> i).toArray();
    }
	
	void printQ() {
        System.out.println("Q matrix");
        for (int i = 0; i < q_table.length; i++) {
            System.out.print("From state " + i + ":  ");
            for (int j = 0; j < q_table[i].length; j++) {
                System.out.printf("%6.2f ", (q_table[i][j]));
            }
            System.out.println();
        }
    }

	//getters - setters
	
	public Location getTarget_loc() {
		return target_loc;
	}
	
	public void setTarget_loc(Location target_loc) {
		this.target_loc = target_loc;
	}

	public double getEpsilon() {
		return epsilon;
	}

	public void setEpsilon(double epsilon) {
		this.epsilon = epsilon;
	}

	public int getExploration_counter() {
		return exploration_counter;
	}

	public void setExploration_counter(int exploration_counter) {
		this.exploration_counter = exploration_counter;
	}

	public boolean isTarget_found() {
		return target_found;
	}

	public void setTarget_found(boolean target_found) {
		this.target_found = target_found;
	}		
	
	

}
