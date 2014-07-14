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
public class CCompareFiles
{

    public static void main(String[] args)
    {
    }
    
	boolean DoCompare(CDynamicTextBuffer pCompareFileBuffer, 
	        CDynamicTextBuffer pToFileBuffer, 
	        CDiffOptions diffOptions, BaseDataObject bIdentical, 
	        BaseDataObject strError)
	{
	    if (pCompareFileBuffer.GetFilePath()
                .equalsIgnoreCase(pToFileBuffer.GetFilePath()))
        {
            strError.setStringValue("The same file is opened!");
            return false;
        }

        if (CUtility.DiffUsingMD4(pCompareFileBuffer.GetFilePath(),
                pToFileBuffer.GetFilePath()))
        {
        	bIdentical.setBooleanValue(true);
            strError.setStringValue("The selected files are identical!");
            if (pCompareFileBuffer.m_bBinaryFile || pToFileBuffer.m_bBinaryFile)
            {
            	strError.setStringValue("The binary files are identical!");
            	return false;
            }
            return true;
        }
        
        // If one file is a binary file, don't diff
        if (pCompareFileBuffer.m_bBinaryFile || pToFileBuffer.m_bBinaryFile)
        {
        	strError.setStringValue("Binary files differ.");
            return false;
        } 
        
    	long[] aryCompareChecksum = new long[pCompareFileBuffer.GetLineCount()];
    	long [] aryToChecksum = new long[pToFileBuffer.GetLineCount()];
    	CUtility.GetChecksum(pCompareFileBuffer, diffOptions, aryCompareChecksum);
    	CUtility.GetChecksum(pToFileBuffer, diffOptions, aryToChecksum); 
        
		CFilesDiff diff = new CFilesDiff();

		int iRet = diff.LCSDiff(aryCompareChecksum, aryToChecksum, m_diffResult, strError);

		switch (iRet)
		{
		case Common.DIFF_OK:
			return true;
		case Common.SAME_FILE:
			bIdentical.setBooleanValue(true);
			return true;
		case Common.DIFF_FAIL:
		case Common.NO_MEMORY:
			return false;
		}

		return false;
	}

	public SDiffResult m_diffResult = new SDiffResult();    
}
