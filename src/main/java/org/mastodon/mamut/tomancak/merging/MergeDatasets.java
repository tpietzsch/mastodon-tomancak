package org.mastodon.mamut.tomancak.merging;

import java.io.File;
import java.io.IOException;

import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.project.MamutProject;
import org.mastodon.mamut.project.MamutProjectIO;
import org.mastodon.model.tag.TagSetStructure;
import org.mastodon.model.tag.TagSetStructure.Tag;
import org.mastodon.model.tag.TagSetStructure.TagSet;

public class MergeDatasets
{
	public static class OutputDataSet
	{
		private File datasetXmlFile;

		private final Model model;

		private final TagSetStructure tagSetStructure;

		private final TagSet conflictTagSet;

		private final TagSet tagConflictTagSet;

		private final TagSet labelConflictTagSet;

		public OutputDataSet()
		{
			this( new Model() );
		}

		public OutputDataSet( final Model model )
		{
			this.model = model;
			tagSetStructure = new TagSetStructure();
			conflictTagSet = tagSetStructure.createTagSet( "Merge Conflict" );
			tagConflictTagSet = tagSetStructure.createTagSet( "Merge Conflict (Tags)" );
			labelConflictTagSet = tagSetStructure.createTagSet( "Merge Conflict (Labels)" );
		}

		public void setDatasetXmlFile( final File file )
		{
			datasetXmlFile = file;
		}

		/**
		 * @param projectRoot
		 * 		where to store the new project
		 */
		public void saveProject( final File projectRoot ) throws IOException
		{
			if ( datasetXmlFile == null )
				throw new IllegalStateException();

			final MamutProject project = new MamutProject( projectRoot, datasetXmlFile );
			try ( final MamutProject.ProjectWriter writer = project.openForWriting() )
			{
				new MamutProjectIO().save( project, writer );
				model.saveRaw( writer );
			}
		}

		public Model getModel()
		{
			return model;
		}

		public Tag addSourceTag( final String name, final int color )
		{
			final TagSet ts = tagSetStructure.createTagSet( "Merge Source " + name );
			final Tag tag = ts.createTag( name, color );
			model.getTagSetModel().setTagSetStructure( tagSetStructure );
			return tag;
		}

		public Tag addConflictTag( final String name, final int color )
		{
			final Tag tag = conflictTagSet.createTag( name, color );
			model.getTagSetModel().setTagSetStructure( tagSetStructure );
			return tag;
		}

		public Tag addTagConflictTag( final String name, final int color )
		{
			final Tag tag = tagConflictTagSet.createTag( name, color );
			model.getTagSetModel().setTagSetStructure( tagSetStructure );
			return tag;
		}

		public Tag addLabelConflictTag( final String name, final int color )
		{
			final Tag tag = labelConflictTagSet.createTag( name, color );
			model.getTagSetModel().setTagSetStructure( tagSetStructure );
			return tag;
		}

		public TagSetStructure getTagSetStructure()
		{
			return tagSetStructure;
		}

		public void updateTagSetModel()
		{
			model.getTagSetModel().setTagSetStructure( tagSetStructure );
		}
	}

	public static void merge( final Dataset dsA, final Dataset dsB, final OutputDataSet output, final double distCutoff, final double mahalanobisDistCutoff, final double ratioThreshold )
	{
		final int minTimepoint = 0;
		final int maxTimepoint = Math.max( dsA.maxNonEmptyTimepoint(), dsB.maxNonEmptyTimepoint() );
		MergeModels.merge( dsA.model(), dsB.model(), output,
				minTimepoint, maxTimepoint,
				distCutoff, mahalanobisDistCutoff, ratioThreshold );
	}
}
