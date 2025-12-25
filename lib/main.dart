import 'package:flutter/material.dart';
import 'screens/home_screen.dart';

void main() {
  runApp(const ForgeAIApp());
}

class ForgeAIApp extends StatelessWidget {
  const ForgeAIApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'ForgeAI',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        useMaterial3: true,
        brightness: Brightness.dark,
        scaffoldBackgroundColor: const Color(0xFF0A0E17),
        colorScheme: const ColorScheme.dark(
          primary: Color(0xFF00F0FF), // Neon Cyan
          secondary: Color(0xFF7000FF), // Neon Violet
          surface: Color(0xFF121624),
          background: Color(0xFF0A0E17),
          onPrimary: Colors.black,
          onSecondary: Colors.white,
        ),
        fontFamily:
            'Roboto', // Default, but can be swapped for something more techy if available
        appBarTheme: const AppBarTheme(
          backgroundColor: Colors.transparent,
          elevation: 0,
          centerTitle: true,
          titleTextStyle: TextStyle(
            fontSize: 24,
            fontWeight: FontWeight.bold,
            letterSpacing: 1.5,
            color: Colors.white,
          ),
        ),
      ),
      home: const HomeScreen(),
    );
  }
}
