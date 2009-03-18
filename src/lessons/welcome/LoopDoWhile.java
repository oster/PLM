package lessons.welcome;

import java.awt.Color;

import jlm.lesson.ExerciseTemplated;
import jlm.lesson.Lesson;
import jlm.universe.Direction;
import universe.bugglequest.Buggle;
import universe.bugglequest.BuggleWorld;

public class LoopDoWhile extends ExerciseTemplated {

	public LoopDoWhile(Lesson lesson) {
		super(lesson);
		tabName = "Program";
				
		BuggleWorld myWorld = new BuggleWorld("Yellow Submarine",7,7);
		for (int i=0;i<7;i++) {
			new Buggle(myWorld, "Beatles"+(i+1), i, 6, Direction.NORTH, Color.black, Color.lightGray);
		    for (int j=6; j>i; j--)
		    	myWorld.setColor(i, j,Color.yellow);
		}
    	myWorld.setColor(6, 6,Color.yellow);

    	setup(myWorld);
	}
}
