/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.ebi.metabolomes.optimise;

import ilog.concert.IloException;
import java.util.logging.Level;
import uk.ac.ebi.optimise.gap.GapFind;
import junit.framework.TestCase;

import org.apache.log4j.Logger;

import org.junit.Test;

import uk.ac.ebi.metabolomes.core.reaction.matrix.BasicStoichiometricMatrix;

import java.lang.reflect.Field;
import java.util.Arrays;

/**
 *
 * @author johnmay
 */
public class DeadEndDetectorTest
    extends TestCase
{
    private static final Logger LOGGER = Logger.getLogger( DeadEndDetectorTest.class );

    //XXX fetch from properties file
    private static final String CPLEX_LIBARY_PATH =
        "/Users/johnmay/ILOG/CPLEX_Studio_AcademicResearch122/cplex//bin/x86-64_darwin9_gcc4.0";
    private BasicStoichiometricMatrix s;

    @Override
    public void setUp(  )
    {
        // try laading the library path for cplex
        try
        {
            System.setProperty( "java.library.path", CPLEX_LIBARY_PATH );

            Field fieldSysPath = ClassLoader.class.getDeclaredField( "sys_paths" );
            fieldSysPath.setAccessible( true );
            fieldSysPath.set( null, null );
        } catch ( IllegalArgumentException ex )
        {
            LOGGER.error( "Could not set CPLEX library path", ex );
        } catch ( IllegalAccessException ex )
        {
            LOGGER.error( "Could not set CPLEX library path", ex );
        } catch ( NoSuchFieldException ex )
        {
            LOGGER.error( "Could not set CPLEX library path", ex );
        } catch ( SecurityException ex )
        {
            LOGGER.error( "Could not set CPLEX library path", ex );
        }

        s = BasicStoichiometricMatrix.create();

        // internal reactions
        s.addReaction( "A => B" );
        s.addReaction( "B => C" );
        s.addReaction( "C => D" );
        s.addReaction( "D => E" );
        s.addReaction( "D => F" );
    }

    /**
     * Test of productionConstraints method, of class DeadEndDetector.
     */
    public void testProductionConstraints(  )
                                   throws Exception
    {
    }

    /**
     * Test of nonProductionMassBalanceConstraint method, of class DeadEndDetector.
     */
    public void testNonProductionMassBalanceConstraint(  )
    {
    }

    /**
     * Test of nonConsumtionMassBalanceConstraint method, of class DeadEndDetector.
     */
    public void testNonConsumtionMassBalanceConstraint(  )
                                                throws Exception
    {
    }

    /**
     * Test of findNonProductionMetabolites method, of class DeadEndDetector.
     */
    public void testFindNonProductionMetabolites(  )
                                          throws Exception
    {
        GapFind deadEndDetector = new GapFind( s );
        Integer[] allNCIndicies = deadEndDetector.getUnproducedMetabolites(  );

        // drain E and F
        s.addReaction( new String[] { "E", "F" },
                       new String[0] );

        assertEquals( 6, allNCIndicies.length );

        for ( Integer i = 0; i < allNCIndicies.length; i++ )
        {
            assertEquals( allNCIndicies[i], i );
        }

        // allow influx of A
        s.addReaction( new String[0],
                       new String[] { "A" } );
        deadEndDetector = new GapFind( s );
        allNCIndicies = deadEndDetector.getUnproducedMetabolites(  );

        // should only be 1 which is F
        assertEquals( 0, allNCIndicies.length );
    }

    /**
     * Test of findNonConsumptionMetabolites method, of class DeadEndDetector.
     */
    public void testFindNonConsumptionMetabolites(  )
                                           throws Exception
    {
        GapFind deadEndDetector = new GapFind( s );
        Integer[] allNCIndicies = deadEndDetector.getUnconsumedMetabolites(  );

        // allow influx of A
        s.addReaction( new String[0],
                       new String[] { "A" } );

        assertEquals( 6, allNCIndicies.length );

        for ( Integer i = 0; i < allNCIndicies.length; i++ )
        {
            assertEquals( allNCIndicies[i], i );
        }

        // drain E and F
        s.addReaction( new String[] { "E" },
                       new String[0] );
        //   s.addReaction( new String[]{ "F" } , new String[ 0 ] );
        deadEndDetector = new GapFind( s );
        allNCIndicies = deadEndDetector.getUnconsumedMetabolites(  );

        // should only be 1 which is F
        assertEquals( 1, allNCIndicies.length );
        assertEquals( "F",
                      s.getMolecule( allNCIndicies[0] ) );
    }

    /**
     * Test of getTerminalNCMetabolites method, of class DeadEndDetector.
     */
    public void testGetTerminalNCMetabolites(  )
    {
        try {
            GapFind deadEndDetector = new GapFind( s );
            Integer[] terminalNCIndicies = deadEndDetector.getRootUnproducedMetabolites(  );

            // there should be 2 root non-production metabolites, E and C
            assertEquals( terminalNCIndicies.length, 2 );
            assertEquals( "E",
                          s.getMolecule( terminalNCIndicies[0] ) );
            assertEquals( "F",
                          s.getMolecule( terminalNCIndicies[1] ) );

            // drain E via exchange reaction
            s.addReaction( new String[] { "E" },
                           new String[0] );
            deadEndDetector = new GapFind( s );
            terminalNCIndicies = deadEndDetector.getRootUnproducedMetabolites(  );

            // there should be one dead end metabolite which is C
            assertEquals( terminalNCIndicies.length, 1 );
            assertEquals( "F",
                          s.getMolecule( terminalNCIndicies[0] ) );
        } catch (IloException ex) {
            java.util.logging.Logger.getLogger(DeadEndDetectorTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsatisfiedLinkError ex) {
            java.util.logging.Logger.getLogger(DeadEndDetectorTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Test of getRootNPMetabolites method, of class DeadEndDetector.
     */
    public void testGetRootNPMetabolites(  )
    {
        try {
            GapFind deadEndDetector = new GapFind( s );
            Integer[] rootNPIndicies = deadEndDetector.getTerminalUnconsumpedMetabolites(  );

            // check there should be 2 root non-production metabolites, A and B
            assertEquals( rootNPIndicies.length, 1 );
            assertEquals( "A",
                          s.getMolecule( rootNPIndicies[0] ) );

            //  produce A via exchange reaction
            s.addReaction( new String[0],
                           new String[] { "A" } );
            deadEndDetector = new GapFind( s );
            rootNPIndicies = deadEndDetector.getTerminalUnconsumpedMetabolites(  );

            // there should be no NP dead end metabolites
            assertEquals( rootNPIndicies.length, 0 );
        } catch (IloException ex) {
            java.util.logging.Logger.getLogger(DeadEndDetectorTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsatisfiedLinkError ex) {
            java.util.logging.Logger.getLogger(DeadEndDetectorTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
