import React from 'react';
import { Container, Typography, Box, Button } from '@mui/material';
import { useNavigate } from 'react-router-dom';

const Home = () => {
  const navigate = useNavigate();

  return (
    <Container maxWidth="lg">
      <Box sx={{ textAlign: 'center', py: 8 }}>
        <Typography variant="h2" component="h1" gutterBottom>
          Welcome to BlogNest
        </Typography>
        <Typography variant="h5" component="h2" gutterBottom color="text.secondary">
          A modern, scalable blog platform built with Spring Boot and React
        </Typography>
        <Typography variant="body1" paragraph sx={{ maxWidth: 600, mx: 'auto', mb: 4 }}>
          Discover amazing stories, share your thoughts, and connect with writers from around the world.
          BlogNest provides a powerful platform for creating, managing, and discovering engaging content.
        </Typography>
        <Box sx={{ display: 'flex', gap: 2, justifyContent: 'center' }}>
          <Button
            variant="contained"
            size="large"
            onClick={() => navigate('/blogs')}
          >
            Explore Blogs
          </Button>
          <Button
            variant="outlined"
            size="large"
            onClick={() => navigate('/register')}
          >
            Get Started
          </Button>
        </Box>
      </Box>
    </Container>
  );
};

export default Home;
