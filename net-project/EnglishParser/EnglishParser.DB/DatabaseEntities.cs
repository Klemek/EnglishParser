
using EnglishParser.Model;
using Microsoft.EntityFrameworkCore;

namespace EnglishParser.DB
{
    public class DatabaseEntities : DbContext
    {
        private static readonly int MAX_WORD_LENGTH = 255;
        private readonly string _connectionString;

        public DatabaseEntities(string connectionString)
        {
            _connectionString = connectionString;
        }

        public virtual DbSet<Word> Words { get; set; }
        public virtual DbSet<Definition> Definitions { get; set; }
        public virtual DbSet<Noun> Nouns { get; set; }
        public virtual DbSet<Verb> Verbs { get; set; }
        public virtual DbSet<Adjective> Adjectives { get; set; }


        protected override void OnConfiguring(DbContextOptionsBuilder optionsBuilder)
        {
            if(!optionsBuilder.IsConfigured)
                optionsBuilder.UseMySQL(_connectionString);
        }

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            modelBuilder.Entity<Word>(entity =>
            {
                entity.ToTable("dict_word");
                entity.Property(e => e.Text)
                    .HasColumnName("word")
                    .IsRequired()
                    .HasMaxLength(MAX_WORD_LENGTH)
                    .IsUnicode(false);
                entity.Property(e => e.Type)
                    .HasColumnName("type")
                    .IsRequired();
                entity.Property(e => e.SynSetId)
                    .HasColumnName("syn_set_id");
                entity.Property(e => e.WordNumber)
                    .HasColumnName("word_num");
                entity.HasKey(e => new {e.SynSetId, e.WordNumber});
            });
            modelBuilder.Entity<Definition>(entity =>
            {
                entity.ToTable("dict_def");
                entity.Property(e => e.SynSetId)
                    .HasColumnName("syn_set_id");
                entity.Property(e => e.Text)
                    .HasColumnName("definition")
                    .IsRequired()
                    .IsUnicode(false);
                entity.HasKey(e => e.SynSetId);
                entity.HasMany(e => e.Synonyms)
                    .WithOne(p => p.Definition)
                    .HasForeignKey(p => p.SynSetId)
                    .OnDelete(DeleteBehavior.Cascade);
            });
            modelBuilder.Entity<Noun>(entity =>
            {
                entity.ToTable("dict_noun");
                entity.Property(e => e.Base)
                    .HasColumnName("base")
                    .IsRequired()
                    .HasMaxLength(MAX_WORD_LENGTH)
                    .IsUnicode(false);
                entity.Property(e => e.Plural)
                    .HasColumnName("plural")
                    .IsRequired()
                    .HasMaxLength(MAX_WORD_LENGTH)
                    .IsUnicode(false);
                entity.Property(e => e.Male)
                    .HasColumnName("male")
                    .HasColumnType("bit")
                    .IsRequired();
                entity.Property(e => e.Female)
                    .HasColumnName("female")
                    .HasMaxLength(MAX_WORD_LENGTH)
                    .IsUnicode(false);
                entity.Property(e => e.FemalePlural)
                    .HasColumnName("female_plural")
                    .HasMaxLength(MAX_WORD_LENGTH)
                    .IsUnicode(false);
                entity.HasKey(e => new {e.Base, e.Plural});
                entity.HasIndex(e => new {e.Base, e.Plural});
                entity.HasIndex(e => e.Female);
            });
            modelBuilder.Entity<Verb>(entity =>
            {
                entity.ToTable("dict_verb");
                entity.Property(e => e.Base)
                    .HasColumnName("base")
                    .IsRequired()
                    .HasMaxLength(MAX_WORD_LENGTH)
                    .IsUnicode(false);
                entity.Property(e => e.PastTense)
                    .HasColumnName("past_tense")
                    .IsRequired()
                    .HasMaxLength(MAX_WORD_LENGTH)
                    .IsUnicode(false);
                entity.Property(e => e.PastParticiple)
                    .HasColumnName("past_part")
                    .IsRequired()
                    .HasMaxLength(MAX_WORD_LENGTH)
                    .IsUnicode(false);
                entity.Property(e => e.PresParticiple)
                    .HasColumnName("pres_part")
                    .IsRequired()
                    .HasMaxLength(MAX_WORD_LENGTH)
                    .IsUnicode(false);
                entity.Property(e => e.ThirdPerson)
                    .HasColumnName("third_pers")
                    .IsRequired()
                    .HasMaxLength(MAX_WORD_LENGTH)
                    .IsUnicode(false);
                entity.HasKey(e => new {e.Base, e.PastTense, e.PastParticiple});
            });
            modelBuilder.Entity<Adjective>(entity =>
            {
                entity.ToTable("dict_adj");
                entity.Property(e => e.Base)
                    .HasColumnName("base")
                    .IsRequired()
                    .HasMaxLength(MAX_WORD_LENGTH)
                    .IsUnicode(false);
                entity.Property(e => e.Adverb)
                    .HasColumnName("adverb")
                    .IsRequired()
                    .HasMaxLength(MAX_WORD_LENGTH)
                    .IsUnicode(false);
                entity.HasKey(e => new {e.Base, e.Adverb});
            });
        }
    }
}