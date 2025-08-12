import React, { useEffect } from 'react';
import { Container, Typography, Box, Grid, Card, CardContent, CardActions, Button, Chip } from '@mui/material';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { fetchBlogs } from '../store/slices/blogSlice';

const Dashboard = () => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { user } = useSelector((state) => state.auth);
  const { blogs, loading } = useSelector((state) => state.blog);

  useEffect(() => {
    if (user) {
      dispatch(fetchBlogs({ page: 1, limit: 10 }));
    }
  }, [dispatch, user]);

  if (!user) {
    return (
      <Container>
        <Typography>Please log in to access your dashboard</Typography>
      </Container>
    );
  }

  const userBlogs = blogs.filter(blog => blog.author === user.username);

  return (
    <Container maxWidth="lg">
      <Box sx={{ py: 4 }}>
        <Typography variant="h3" component="h1" gutterBottom>
          Dashboard
        </Typography>
        
        <Typography variant="h5" component="h2" gutterBottom color="text.secondary">
          Welcome back, {user.firstName || user.username}!
        </Typography>

        <Grid container spacing={3} sx={{ mt: 2 }}>
          <Grid item xs={12} md={4}>
            <Card>
              <CardContent>
                <Typography variant="h6" component="h3" gutterBottom>
                  Your Blogs
                </Typography>
                <Typography variant="h3" component="p" color="primary">
                  {userBlogs.length}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Total blog posts published
                </Typography>
              </CardContent>
              <CardActions>
                <Button size="small" onClick={() => navigate('/create-blog')}>
                  Create New Blog
                </Button>
              </CardActions>
            </Card>
          </Grid>

          <Grid item xs={12} md={4}>
            <Card>
              <CardContent>
                <Typography variant="h6" component="h3" gutterBottom>
                  Profile Status
                </Typography>
                <Typography variant="body1" component="p">
                  {user.firstName && user.lastName ? 'Complete' : 'Incomplete'}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Profile information
                </Typography>
              </CardContent>
              <CardActions>
                <Button size="small" onClick={() => navigate('/profile')}>
                  Update Profile
                </Button>
              </CardActions>
            </Card>
          </Grid>

          <Grid item xs={12} md={4}>
            <Card>
              <CardContent>
                <Typography variant="h6" component="h3" gutterBottom>
                  Account Type
                </Typography>
                <Chip label={user.role} color="primary" />
                <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                  User role and permissions
                </Typography>
              </CardContent>
            </Card>
          </Grid>
        </Grid>

        <Box sx={{ mt: 4 }}>
          <Typography variant="h4" component="h2" gutterBottom>
            Your Recent Blogs
          </Typography>
          
          {loading ? (
            <Typography>Loading...</Typography>
          ) : userBlogs.length > 0 ? (
            <Grid container spacing={3}>
              {userBlogs.slice(0, 6).map((blog) => (
                <Grid item xs={12} md={6} key={blog.id}>
                  <Card>
                    <CardContent>
                      <Typography variant="h6" component="h3" gutterBottom>
                        {blog.title}
                      </Typography>
                      <Typography variant="body2" color="text.secondary" paragraph>
                        {blog.excerpt || blog.content.substring(0, 100)}...
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        {new Date(blog.createdAt).toLocaleDateString()}
                      </Typography>
                    </CardContent>
                    <CardActions>
                      <Button size="small" onClick={() => navigate(`/blogs/${blog.id}`)}>
                        View
                      </Button>
                      <Button size="small" onClick={() => navigate(`/edit-blog/${blog.id}`)}>
                        Edit
                      </Button>
                    </CardActions>
                  </Card>
                </Grid>
              ))}
            </Grid>
          ) : (
            <Box sx={{ textAlign: 'center', py: 4 }}>
              <Typography variant="h6" color="text.secondary" gutterBottom>
                You haven't published any blogs yet
              </Typography>
              <Button
                variant="contained"
                onClick={() => navigate('/create-blog')}
                sx={{ mt: 2 }}
              >
                Create Your First Blog
              </Button>
            </Box>
          )}
        </Box>
      </Box>
    </Container>
  );
};

export default Dashboard;
