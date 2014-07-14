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
 * Created on 2006-4-28
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package MergeHeroLib;

import com.dynamsoft.sourceanywhere.BaseDataObject;

/**
 * @author Falcon Young
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class CDynamicFilesDiffAndMerge
{

  public static void main(String[] args)
  {
  }
 
  private CDiffMergeBlockArray m_aryDiffMergeBlock = new CDiffMergeBlockArray();
  
  // All CDynamicTextBuffer objects will contains all file contents;
  // When you call the following functions, it's ok to construct a CDynamicTextBuffer object with file full name
  // Of course, you can call SetFilePath function in CDynamicTextBuffer later
  public boolean CompareFiles(CDynamicTextBuffer compareFileBuffer, 
      CDynamicTextBuffer toFileBuffer, CDiffOptions diffOptions, 
      BaseDataObject bIdentical, BaseDataObject strError)
  {
  	// If not show identical files, not need load file
  	if (!diffOptions.m_bShowIdenticalFiles)
  	{
  		if (CUtility.DiffUsingMD4(compareFileBuffer.GetFilePath(), toFileBuffer.GetFilePath()))
  		{
  			bIdentical.setBooleanValue(true);
 			return true;
  		}
  	}

  	// Load files
  	if (!CUtility.LoadFile(compareFileBuffer, strError) ||
  	        !CUtility.LoadFile(toFileBuffer, strError))
  	{
  		return false;
  	}

  	CCompareFiles compare = new CCompareFiles();

  	// Compare files
  	if (!compare.DoCompare(compareFileBuffer, toFileBuffer, diffOptions, bIdentical, strError))
  	{
  		return false;
  	}

  	// Deal with diff result
  	CUtility.PrimeTextBuffersForDiff(compareFileBuffer, toFileBuffer, compare.m_diffResult, m_aryDiffMergeBlock);
  	
  	if ((compareFileBuffer.m_ulNumDeletedLines == 0) && (toFileBuffer.m_ulNumInsertedLines == 0))
  	{
  		bIdentical.setBooleanValue(true);
  	}

    return true;
  }
  
  public boolean TwoWayMerge(CDynamicTextBuffer compareFileBuffer, 
      CDynamicTextBuffer toFileBuffer, CDynamicTextBuffer resultBuffer, 
      CDiffOptions diffOptions, BaseDataObject strError)
  {
  	// Load files
	if (!CUtility.LoadFile(compareFileBuffer, strError) ||
	        !CUtility.LoadFile(toFileBuffer, strError))
	{
		return false;
	}

  	CCompareFiles compare = new CCompareFiles();
  	BaseDataObject bIdentical = new BaseDataObject();

  	// Compare files
  	if (!compare.DoCompare(compareFileBuffer, toFileBuffer, diffOptions, 
  		bIdentical, strError))
  	{
  		return false;
  	}

  	// Deal with diff result
  	CUtility.PrimeTextBuffersForTwoWayMerge(compareFileBuffer, toFileBuffer,
  		resultBuffer, compare.m_diffResult, m_aryDiffMergeBlock);

    return true;
  }
	
  // If bAutoMerge = true, all buffers may be not contain data, so you can't use data in buffer to visual merge
  // The merge result will be saved to the file which be specified in pResultBuffer directly
  public boolean ThreeWayMerge(CDynamicTextBuffer baseBuffer, CDynamicTextBuffer theirsBuffer, 
      CDynamicTextBuffer yoursBuffer, CDynamicTextBuffer resultBuffer, 
      CDiffOptions diffOptions, boolean bAutoMerge, BaseDataObject bHasConflict, BaseDataObject strError)
  {
  	if (bAutoMerge)
	{// Try to auto merge first, because it may be has two files are identical
		BaseDataObject bHasError = new BaseDataObject();
		if (CUtility.TryToAutoMerge(baseBuffer, theirsBuffer, yoursBuffer,resultBuffer, bHasError, strError))
		{
			bHasConflict.setBooleanValue(false);
			return !bHasError.getBooleanValue();
		}
	}

	// Load files
	if (!CUtility.LoadFile(baseBuffer, strError) ||
	        !CUtility.LoadFile(yoursBuffer, strError) ||
	        !CUtility.LoadFile(theirsBuffer, strError))
	{
		return false;
	}

	CCompareFiles compare = new CCompareFiles(), compare1 = new CCompareFiles(), compare2 = new CCompareFiles();
	BaseDataObject bIdentical = new BaseDataObject(), bIdentical1 = new BaseDataObject(), bIdentical2 = new BaseDataObject();

	// Compare base and theirs
	if (!compare1.DoCompare(baseBuffer, theirsBuffer, diffOptions, 
		bIdentical1, strError))
	{
		return false;
	}

	// Compare base and yours
	if (!compare2.DoCompare(baseBuffer, yoursBuffer, diffOptions, 
		bIdentical2, strError))
	{
		return false;
	}

	// Recombine the two diff results for compare again
	CUtility.ReCombineDiffResult(baseBuffer, theirsBuffer, yoursBuffer, compare1.m_diffResult, compare2.m_diffResult);
	
	// Compare again
	if (!compare.DoCompare(theirsBuffer, yoursBuffer, diffOptions, 
		bIdentical, strError))
	{
		return false;
	}
	
	// Deal with diff result
	CUtility.PrimeTextBuffersForThreeWayMerge(theirsBuffer, yoursBuffer, resultBuffer, compare.m_diffResult, 
		m_aryDiffMergeBlock);

	bHasConflict.setBooleanValue((resultBuffer.m_ulNumConflictLines != 0));
	if (!bHasConflict.getBooleanValue())
	{
		resultBuffer.SaveFile(strError);
	}
	
    return true;
  }
	
  // Get diff or merge result
  public void GetDiffMergeBlock(CDiffMergeBlockArray aryDiffMergeBlock)
  {
    aryDiffMergeBlock.clear();
    aryDiffMergeBlock.addAll(m_aryDiffMergeBlock);
  }
}
