using System.Data.Common;
using System.Data.Entity;
using System.Data.Entity.Infrastructure;
using EnglishParser.Core.Model;
using MySql.Data.EntityFramework;

namespace EnglishParser.DB
{
    [DbConfigurationType(typeof(MySqlEFConfiguration))]
    public class DatabaseEntities : DbContext
    {
        public DatabaseEntities(DbConnection existingConnection) : base(existingConnection, true)
        {
        }

        protected override void OnModelCreating(DbModelBuilder modelBuilder)
        {
            throw new UnintentionalCodeFirstException();
        }
        
        public virtual DbSet<Word> Words { get; set; }
        public virtual DbSet<Definition> Definitions { get; set; }
        public virtual DbSet<Noun> Nouns { get; set; }
        public virtual DbSet<Verb> Verbs { get; set; }
        public virtual DbSet<Adjective> Adjectives { get; set; }
    }
}