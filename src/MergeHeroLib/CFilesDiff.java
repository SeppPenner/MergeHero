/*
 * MergeHero: Differing and Merging Folders & Files
 *
 * Copyright © 2004, Dynamsoft, Inc. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.opensource.org/licenses/gpl-3.0.html.
 */

/*
 * Created on 2005-4-13
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * @author Falcon Young
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

package MergeHeroLib;
import java.util.ArrayList;
import com.dynamsoft.sourceanywhere.*;

public class CFilesDiff
{
    public static void main(String[] args)
    {
    }
    

    class SDiffData
    {
    	long[] m_aryChecksum;	// Buffer of checksum that will be compared
    	int m_lLength;				// Number of elements (lines)

        // Array of booleans that flag for modified data.
        // This is the result of the diff.
        // This means deletedA in the first Data or inserted in the second Data.
    	boolean[] m_aryModified;

    };

//     Shortest Middle Snake Return Data
    class SMSRD
    {
          int x, y; 

    	  SMSRD()
    	  {
    		  x = y = 0;
    	  }

    };
    

    //	Apply the greedy lcs/ses algorithm between X and Y sequence
    //  equal is a function to compare X and Y which must return 0 if
    //  X and Y are different, 1 if they are identical
    //  return a list of matched pairs in tuplesthe greedy lcs/ses algorithm
    public int LCSDiff(long[] aryCompareChecksum, 
			long[] aryToChecksum, 
	        SDiffResult pDiffResult,
            BaseDataObject pstrError)
    {
        int m_lDiffCount;

        int lOrgLinesCount = aryCompareChecksum.length; // the original
                                                                // file lines
                                                                // count
        int lDestLinesCount = aryToChecksum.length; // the
                                                                  // destination
                                                                  // file lines
                                                                  // count
 
    	SDiffData diffDataA = new SDiffData(), diffDataB = new SDiffData();

    	diffDataA.m_aryChecksum = aryCompareChecksum;
    	diffDataA.m_lLength = lOrgLinesCount;
    	diffDataA.m_aryModified = new boolean[lOrgLinesCount + 2];

    	for (int i = 0; i < diffDataA.m_aryModified.length; i++)
    	{
    		diffDataA.m_aryModified[i] = false;
    	}
    	
    	diffDataB.m_aryChecksum = aryToChecksum;
    	diffDataB.m_lLength = lDestLinesCount;
    	diffDataB.m_aryModified = new boolean[lDestLinesCount + 2];

    	for (int i = 0; i < diffDataB.m_aryModified.length; i++)
    	{
    		diffDataB.m_aryModified[i] = false;
    	}

   		LCS(diffDataA, 0, diffDataA.m_lLength, diffDataB, 0, diffDataB.m_lLength);

    	GetDifferences(diffDataA, diffDataB, pDiffResult);
  
    	return Common.DIFF_OK;
    }

void LCS(SDiffData diffDataA, int iLowerA, int iUpperA, SDiffData diffDataB, int iLowerB, int iUpperB)
{
	// Fast walkthrough equal lines at the start
	while ((iLowerA < iUpperA) && (iLowerB < iUpperB) && (diffDataA.m_aryChecksum[iLowerA] == diffDataB.m_aryChecksum[iLowerB])) 
	{
		iLowerA++; iLowerB++;
	}

	// Fast walkthrough equal lines at the end
	while ((iLowerA < iUpperA) && (iLowerB < iUpperB) && (diffDataA.m_aryChecksum[iUpperA-1] == diffDataB.m_aryChecksum[iUpperB-1])) 
	{
		--iUpperA; --iUpperB;
	}

	if (iLowerA == iUpperA)
	{
		// mark as inserted lines.
		while (iLowerB < iUpperB)
		{
			diffDataB.m_aryModified[iLowerB++] = true;
		}

	} 
	else if (iLowerB == iUpperB) 
	{
		// mark as deleted lines.
		while (iLowerA < iUpperA)
		{
			diffDataA.m_aryModified[iLowerA++] = true;
		}

	} 
	else
	{
		// Find the middle snakea and length of an optimal path for A and B
		SMSRD smsrd = SMS(diffDataA, iLowerA, iUpperA, diffDataB, iLowerB, iUpperB);

		// The path is from LowerX to (x,y) and (x,y) ot UpperX
		LCS(diffDataA, iLowerA, smsrd.x, diffDataB, iLowerB, smsrd.y);
		LCS(diffDataA, smsrd.x, iUpperA, diffDataB, smsrd.y, iUpperB);  
	}
}

// This is the algorithm to find the Shortest Middle Snake (SMS).
// <param name="diffDataA">sequence A</param>
// <param name="iLowerA">lower bound of the actual range in diffDataA</param>
// <param name="iUpperA">upper bound of the actual range in diffDataA (exclusive)</param>
// <param name="diffDataB">sequence B</param>
// <param name="iLowerB">lower bound of the actual range in diffDataB</param>
// <param name="iUpperB">upper bound of the actual range in diffDataB (exclusive)</param>
// <returns>a MiddleSnakeData record containing x,y and u,v</returns>
SMSRD SMS(SDiffData diffDataA, int iLowerA, int iUpperA, SDiffData diffDataB, int iLowerB, int iUpperB) 
{
    SMSRD ret = new SMSRD();
    int MAX = (int) (diffDataA.m_lLength + diffDataB.m_lLength + 1);

    int DownK = iLowerA - iLowerB; // the k-line to start the forward search
    int UpK = iUpperA - iUpperB; // the k-line to start the reverse search

    int Delta = (iUpperA - iLowerA) - (iUpperB - iLowerB);
    boolean oddDelta = (Delta & 1) != 0;

    /// vector for the (0,0) to (x,y) search
    int[]  pDownVector = new int[2* MAX + 2];

    /// vector for the (u,v) to (N,M) search
    int[] pUpVector = new int[2 * MAX + 2];

	if (null == pDownVector || null == pUpVector)
	{
		assert(false);
		return ret;
	}
    
    // The vectors in the publication accepts negative indexes. the vectors implemented here are 0-based
    // and are access using a specific offset: UpOffset pUpVector and DownOffset for DownVektor
    int DownOffset = MAX - DownK;
    int UpOffset = MAX - UpK;

    int  MaxD = ((iUpperA - iLowerA + iUpperB - iLowerB) / 2) + 1;
	
    // init vectors
    pDownVector[DownOffset + DownK + 1] = iLowerA;
    pUpVector[UpOffset + UpK - 1] = iUpperA;
		
	for (int D = 0; D <= MaxD; D++)
	{

		// Extend the forward path.
		for (int k = DownK - D; k <= DownK + D; k += 2) 
		{
			// find the only or better starting point
			int x, y;
			if (k == DownK - D) 
			{
				x = pDownVector[DownOffset + k+1]; // down
			} 
			else
			{
				x = pDownVector[DownOffset + k-1] + 1; // a step to the right
				if ((k < DownK + D) && (pDownVector[DownOffset + k+1] >= x))
				{
					x = pDownVector[DownOffset + k+1]; // down
				}
			}
			y = x - k;

			// find the end of the furthest reaching forward D-path in diagonal k.
			while ((x < iUpperA) && (y < iUpperB) && (diffDataA.m_aryChecksum[x] == diffDataB.m_aryChecksum[y])) 
			{
				x++; y++;
			}
			pDownVector[DownOffset + k] = x;

			// overlap ?
			if (oddDelta && (UpK-D < k) && (k < UpK+D)) 
			{
				if (pUpVector[UpOffset + k] <= pDownVector[DownOffset + k]) 
				{
					ret.x = pDownVector[DownOffset + k];
					ret.y = pDownVector[DownOffset + k] - k;
					return (ret);
				} // if
			} // if

		} // for k

		// Extend the reverse path.
		for (int k = UpK - D; k <= UpK + D; k += 2)
		{
			// find the only or better starting point
			int x, y;
			if (k == UpK + D)
			{
				x = pUpVector[UpOffset + k-1]; // up
			} 
			else 
			{
				x = pUpVector[UpOffset + k+1] - 1; // left
				if ((k > UpK - D) && (pUpVector[UpOffset + k-1] < x))
				{
					x = pUpVector[UpOffset + k-1]; // up
				}
			} // if
			y = x - k;

			while ((x > iLowerA) && (y > iLowerB) && (diffDataA.m_aryChecksum[x-1] == diffDataB.m_aryChecksum[y-1])) 
			{
				x--; y--; // diagonal
			}
			pUpVector[UpOffset + k] = x;

			// overlap ?
			if (! oddDelta && (DownK-D <= k) && (k <= DownK+D)) 
			{
				if (pUpVector[UpOffset + k] <= pDownVector[DownOffset + k])
				{
					ret.x = pDownVector[DownOffset + k];
					ret.y = pDownVector[DownOffset + k] - k;
					return (ret);
				} // if
			} // if

		} // for k

	} // for D

    // the algorithm should never come here
	assert(false);

	return (ret);
} // SMS

void GetDifferences(SDiffData diffDataA, SDiffData diffDataB, SDiffResult pDiffResult)
{
	int iStartA, iStartB;
	int iLineA, iLineB;

	iLineA = 0;
	iLineB = 0;
	boolean bHasMatchedLines = false;
	int iMatchedLines = 0;

	while ((iLineA < diffDataA.m_lLength) || (iLineB < diffDataB.m_lLength)) 
	{
		if ((iLineA < diffDataA.m_lLength) && (!diffDataA.m_aryModified[iLineA])
			&& (iLineB < diffDataB.m_lLength) && (!diffDataB.m_aryModified[iLineB])) 
		{
			// equal lines
			iLineA++; 
			iLineB++;
			
			iMatchedLines ++;
			bHasMatchedLines = true;
		} 
		else 
		{
			// maybe deleted and/or inserted lines
			iStartA = iLineA;
			iStartB = iLineB;

			while ((iLineA < diffDataA.m_lLength) && (iLineB >= diffDataB.m_lLength || diffDataA.m_aryModified[iLineA]))
			{
				// while (iLineA < diffDataA.m_lLength && diffDataA.modified[iLineA])
				iLineA++;
			}

			while ((iLineB < diffDataB.m_lLength) && (iLineA >= diffDataA.m_lLength || diffDataB.m_aryModified[iLineB]))
			{
				// while (iLineB < diffDataB.m_lLength && diffDataB.modified[iLineB])
				iLineB++;
			}

			if ((iStartA < iLineA) || (iStartB < iLineB))
			{
				// Only store matched lines
				if (bHasMatchedLines)
				{
					CDiffResult diffMatchResult = new CDiffResult();

					diffMatchResult.m_lOrgMatchFrom = iStartA - iMatchedLines + 1;
					diffMatchResult.m_lOrgMatchTo = iStartA;
					diffMatchResult.m_lDestMatchFrom = iStartB - iMatchedLines + 1;
					diffMatchResult.m_lDestMatchTo = iStartB;

					diffMatchResult.m_lOrgLinesCount = diffDataA.m_lLength;
					diffMatchResult.m_lDestLinesCount = diffDataB.m_lLength;

					pDiffResult.m_aryDiffResult.add(diffMatchResult);

					bHasMatchedLines = false;
					iMatchedLines = 0;
				}
			} // if
		} // if
	} // while	

	// Only store matched lines
	if (bHasMatchedLines)
	{
		CDiffResult diffMatchResult = new CDiffResult();

		diffMatchResult.m_lOrgMatchFrom = iLineA - iMatchedLines + 1;
		diffMatchResult.m_lOrgMatchTo = iLineA;
		diffMatchResult.m_lDestMatchFrom = iLineB - iMatchedLines + 1;
		diffMatchResult.m_lDestMatchTo = iLineB;

		diffMatchResult.m_lOrgLinesCount = diffDataA.m_lLength;
		diffMatchResult.m_lDestLinesCount = diffDataB.m_lLength;

		pDiffResult.m_aryDiffResult.add(diffMatchResult);

		bHasMatchedLines = false;
		iMatchedLines = 0;
	}
	
	if (pDiffResult.m_aryDiffResult.size() == 0)
	{// No match lines
		CDiffResult diffResult = new CDiffResult();
		
		diffResult.m_lOrgLinesCount = diffDataA.m_lLength;
		diffResult.m_lDestLinesCount = diffDataB.m_lLength;

		pDiffResult.m_aryDiffResult.add(diffResult);
	}	

	// Get difference lines
	GetDifferencesLast(pDiffResult);
}

void GetDifferencesLast(SDiffResult pDiffResult)
{
	int iSize = (int)(pDiffResult.m_aryDiffResult.size());

	CDiffResult pPrev = null;
	CDiffResult pTempDiffResult = null;

	for (int i = 0; i < iSize; i++)
	{
		pTempDiffResult = (CDiffResult)pDiffResult.m_aryDiffResult.get(i);
		
		assert (null != pTempDiffResult);
		
		// link the previous diff resoult
		pTempDiffResult.m_pPrev = pPrev;
		pPrev = pTempDiffResult;

		// count the edit script
		pTempDiffResult.GetEditScript();
	}

	// Get the last diff result
	pTempDiffResult = new CDiffResult();
	if (null != pTempDiffResult)
	{
		pTempDiffResult.m_pPrev = pPrev;
		pTempDiffResult.GetLastEditScript();
		
		pDiffResult.m_aryDiffResult.add(pTempDiffResult);
	}
}
}