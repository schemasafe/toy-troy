
import com.datastax.driver.core.{Cluster, Session}
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

import troy.api._

// Try changing the table name below
// or change any of the columns names/types
@schema object Schema extends SchemaFromString("""
  CREATE TABLE posts (
    author_name text,
    post_id int,
    post_rating int,
    post_tags list<text>,
    post_title text,
    reviewer_name text,
    PRIMARY KEY (author_name, post_id)
  );
  """)

class PostService(implicit session: Session) {
  import Schema._

  // Try changing the table name below
  // or change any of the columns names
  @schemasafe val listByAuthor = query[PostService.Queries.Get, Post](
    """
      SELECT author_name, post_id, reviewer_name, post_title, post_rating, post_tags
      FROM posts
      WHERE author_name = ? AND post_id = ?
    """
  )
}

object PostService {
  object Queries {
    // Try messing around with the types of fields
    final case class Get(authorName: String, postId: Int)
  }
}

case class Post(
  authorName: String,
  postId: Int,
  reviewerName: String,
  title: String,
  rating: Int,
  tags: Seq[String]
)

object Example extends App {
  val port: Int = 9042
  val host: String = "127.0.0.1"

  private val cluster =
    new Cluster.Builder().addContactPoints(host).withPort(port).build()

  implicit val session: Session = cluster.connect("test")

  val postService = new PostService()

  val postsF = postService.listByAuthor(PostService.Queries.Get("Tam", 1))
  val posts = Await.result(postsF, Duration(1, "second"))
  println(posts)

  session.close()
  cluster.close()
}

// Enter this in cqlsh (assuming a keyspace called test, and table as defined in schema)
//INSERT INTO test.posts ( author_name, post_id, reviewer_name, post_title, post_rating, post_tags ) VALUES ( 'Tam', 1, 'rev', 'title', 5, ['scala', 'troy']) ;
