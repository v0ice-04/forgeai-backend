import 'dart:ui';
import 'package:flutter/material.dart';
import '../models/generation_model.dart';
import '../services/forge_service.dart';
import 'preview_screen.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  final _formKey = GlobalKey<FormState>();
  final _forgeService = ForgeService();

  String _applicationType = 'Website';
  final _projectNameController = TextEditingController();
  final _descriptionController = TextEditingController();
  String _category = 'Portfolio';
  final List<String> _selectedSections = [];
  String _themeColor = 'Blue';
  bool _isLoading = false;

  final List<String> _categories = ['Portfolio', 'Business', 'Landing Page'];
  final List<String> _availableSections = [
    'Home',
    'About',
    'Services',
    'Projects',
    'Contact'
  ];
  final Map<String, Color> _themeColors = {
    'Blue': Colors.blue,
    'Red': Colors.red,
    'Green': Colors.green,
    'Purple': Colors.purple,
    'Orange': Colors.orange,
  };

  Future<void> _generateApplication() async {
    if (!_formKey.currentState!.validate()) return;
    if (_selectedSections.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Please select at least one section')),
      );
      return;
    }

    setState(() => _isLoading = true);

    try {
      final request = GenerationRequest(
        applicationType: _applicationType,
        projectName: _projectNameController.text,
        description: _descriptionController.text,
        category: _category,
        sections: _selectedSections,
        themeColor: _themeColor,
      );

      final response = await _forgeService.generateApplication(request);

      if (!mounted) return;

      Navigator.push(
        context,
        MaterialPageRoute(
          builder: (context) => PreviewScreen(response: response),
        ),
      );
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(e.toString())),
      );
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      extendBodyBehindAppBar: true,
      appBar: AppBar(
        title: Column(
          children: [
            const Text('FORGE AI', style: TextStyle(letterSpacing: 2.0)),
            Text(
              'Build applications with AI',
              style: TextStyle(
                fontSize: 10,
                color: Colors.white.withOpacity(0.7),
                letterSpacing: 1.0,
              ),
            ),
          ],
        ),
      ),
      body: Stack(
        children: [
          // Background Gradient
          Container(
            decoration: const BoxDecoration(
              gradient: LinearGradient(
                begin: Alignment.topLeft,
                end: Alignment.bottomRight,
                colors: [
                  Color(0xFF0A0E17),
                  Color(0xFF1A1F35),
                  Color(0xFF0A0E17),
                ],
              ),
            ),
          ),
          // Subtle Glow Orbs
          Positioned(
            top: -100,
            left: -100,
            child: _buildGlowOrb(const Color(0xFF00F0FF)),
          ),
          Positioned(
            bottom: -100,
            right: -100,
            child: _buildGlowOrb(const Color(0xFF7000FF)),
          ),
          // Content
          SafeArea(
            child: _isLoading
                ? _buildLoadingState()
                : SingleChildScrollView(
                    padding: const EdgeInsets.all(24.0),
                    child: Form(
                      key: _formKey,
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.stretch,
                        children: [
                          _buildModuleSelector(),
                          const SizedBox(height: 32),
                          _buildGlassCard(
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                _buildSectionTitle('Project Details'),
                                const SizedBox(height: 16),
                                _buildNeonTextField(
                                  controller: _projectNameController,
                                  label: 'Project Name',
                                  icon: Icons.rocket_launch,
                                ),
                                const SizedBox(height: 16),
                                _buildNeonTextField(
                                  controller: _descriptionController,
                                  label: 'Description',
                                  icon: Icons.description,
                                  maxLines: 3,
                                ),
                                const SizedBox(height: 24),
                                _buildSectionTitle('Configuration'),
                                const SizedBox(height: 16),
                                _buildDropdown(),
                                const SizedBox(height: 24),
                                _buildSectionTitle('Sections'),
                                const SizedBox(height: 12),
                                _buildSectionSelector(),
                                const SizedBox(height: 24),
                                _buildSectionTitle('Theme'),
                                const SizedBox(height: 12),
                                _buildThemeSelector(),
                              ],
                            ),
                          ),
                          const SizedBox(height: 32),
                          _buildGenerateButton(),
                        ],
                      ),
                    ),
                  ),
          ),
        ],
      ),
    );
  }

  Widget _buildGlowOrb(Color color) {
    return Container(
      width: 300,
      height: 300,
      decoration: BoxDecoration(
        shape: BoxShape.circle,
        color: color.withOpacity(0.15),
        boxShadow: [
          BoxShadow(
            color: color.withOpacity(0.2),
            blurRadius: 100,
            spreadRadius: 50,
          ),
        ],
      ),
    );
  }

  Widget _buildGlassCard({required Widget child}) {
    return ClipRRect(
      borderRadius: BorderRadius.circular(24),
      child: BackdropFilter(
        filter: ImageFilter.blur(sigmaX: 10, sigmaY: 10),
        child: Container(
          padding: const EdgeInsets.all(24),
          decoration: BoxDecoration(
            color: Colors.white.withOpacity(0.05),
            borderRadius: BorderRadius.circular(24),
            border: Border.all(
              color: Colors.white.withOpacity(0.1),
              width: 1,
            ),
          ),
          child: child,
        ),
      ),
    );
  }

  Widget _buildModuleSelector() {
    return Container(
      padding: const EdgeInsets.all(4),
      decoration: BoxDecoration(
        color: Colors.black.withOpacity(0.3),
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: Colors.white.withOpacity(0.1)),
      ),
      child: Row(
        children: [
          Expanded(
            child: _buildModuleTab('Website', true),
          ),
          Expanded(
            child: _buildModuleTab('Mobile App', false),
          ),
        ],
      ),
    );
  }

  Widget _buildModuleTab(String title, bool isSelected) {
    return Container(
      padding: const EdgeInsets.symmetric(vertical: 12),
      decoration: BoxDecoration(
        color: isSelected
            ? const Color(0xFF00F0FF).withOpacity(0.2)
            : Colors.transparent,
        borderRadius: BorderRadius.circular(12),
        border: isSelected
            ? Border.all(color: const Color(0xFF00F0FF).withOpacity(0.5))
            : null,
      ),
      child: Center(
        child: Text(
          title,
          style: TextStyle(
            color: isSelected ? const Color(0xFF00F0FF) : Colors.grey,
            fontWeight: FontWeight.bold,
            letterSpacing: 1.0,
          ),
        ),
      ),
    );
  }

  Widget _buildSectionTitle(String title) {
    return Text(
      title.toUpperCase(),
      style: const TextStyle(
        color: Color(0xFF00F0FF),
        fontSize: 12,
        fontWeight: FontWeight.bold,
        letterSpacing: 2.0,
      ),
    );
  }

  Widget _buildNeonTextField({
    required TextEditingController controller,
    required String label,
    required IconData icon,
    int maxLines = 1,
  }) {
    return TextFormField(
      controller: controller,
      maxLines: maxLines,
      style: const TextStyle(color: Colors.white),
      decoration: InputDecoration(
        labelText: label,
        labelStyle: TextStyle(color: Colors.white.withOpacity(0.5)),
        prefixIcon: Icon(icon, color: const Color(0xFF00F0FF)),
        filled: true,
        fillColor: Colors.black.withOpacity(0.2),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: BorderSide(color: Colors.white.withOpacity(0.1)),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: const BorderSide(color: Color(0xFF00F0FF), width: 2),
        ),
      ),
      validator: (value) => value?.isEmpty ?? true ? 'Required' : null,
    );
  }

  Widget _buildDropdown() {
    return DropdownButtonFormField<String>(
      value: _category,
      dropdownColor: const Color(0xFF1A1F35),
      style: const TextStyle(color: Colors.white),
      decoration: InputDecoration(
        labelText: 'Category',
        labelStyle: TextStyle(color: Colors.white.withOpacity(0.5)),
        prefixIcon: const Icon(Icons.category, color: Color(0xFF00F0FF)),
        filled: true,
        fillColor: Colors.black.withOpacity(0.2),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: BorderSide(color: Colors.white.withOpacity(0.1)),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: const BorderSide(color: Color(0xFF00F0FF), width: 2),
        ),
      ),
      items: _categories.map((c) {
        return DropdownMenuItem(value: c, child: Text(c));
      }).toList(),
      onChanged: (value) {
        if (value != null) setState(() => _category = value);
      },
    );
  }

  Widget _buildSectionSelector() {
    return Wrap(
      spacing: 12,
      runSpacing: 12,
      children: _availableSections.map((section) {
        final isSelected = _selectedSections.contains(section);
        return FilterChip(
          label: Text(section),
          selected: isSelected,
          onSelected: (selected) {
            setState(() {
              if (selected) {
                _selectedSections.add(section);
              } else {
                _selectedSections.remove(section);
              }
            });
          },
          backgroundColor: Colors.black.withOpacity(0.2),
          selectedColor: const Color(0xFF7000FF).withOpacity(0.3),
          checkmarkColor: const Color(0xFF00F0FF),
          labelStyle: TextStyle(
            color: isSelected ? Colors.white : Colors.white.withOpacity(0.6),
            fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
          ),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(20),
            side: BorderSide(
              color: isSelected
                  ? const Color(0xFF7000FF)
                  : Colors.white.withOpacity(0.1),
            ),
          ),
        );
      }).toList(),
    );
  }

  Widget _buildThemeSelector() {
    return Wrap(
      spacing: 16,
      children: _themeColors.entries.map((entry) {
        final isSelected = _themeColor == entry.key;
        return GestureDetector(
          onTap: () => setState(() => _themeColor = entry.key),
          child: Container(
            width: 40,
            height: 40,
            decoration: BoxDecoration(
              shape: BoxShape.circle,
              gradient: LinearGradient(
                colors: [entry.value, entry.value.withOpacity(0.7)],
                begin: Alignment.topLeft,
                end: Alignment.bottomRight,
              ),
              boxShadow: isSelected
                  ? [
                      BoxShadow(
                        color: entry.value.withOpacity(0.5),
                        blurRadius: 12,
                        spreadRadius: 2,
                      )
                    ]
                  : null,
              border:
                  isSelected ? Border.all(color: Colors.white, width: 2) : null,
            ),
          ),
        );
      }).toList(),
    );
  }

  Widget _buildGenerateButton() {
    return Container(
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(16),
        boxShadow: [
          BoxShadow(
            color: const Color(0xFF00F0FF).withOpacity(0.3),
            blurRadius: 20,
            spreadRadius: 0,
          ),
        ],
      ),
      child: ElevatedButton(
        onPressed: _generateApplication,
        style: ElevatedButton.styleFrom(
          backgroundColor: Colors.transparent,
          shadowColor: Colors.transparent,
          padding: const EdgeInsets.symmetric(vertical: 20),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(16),
          ),
        ).copyWith(
          backgroundColor: MaterialStateProperty.all(Colors.transparent),
        ),
        child: Ink(
          decoration: BoxDecoration(
            gradient: const LinearGradient(
              colors: [Color(0xFF00F0FF), Color(0xFF7000FF)],
              begin: Alignment.topLeft,
              end: Alignment.bottomRight,
            ),
            borderRadius: BorderRadius.circular(16),
          ),
          child: Container(
            alignment: Alignment.center,
            constraints: const BoxConstraints(minHeight: 50),
            child: const Text(
              'GENERATE APPLICATION',
              style: TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.bold,
                letterSpacing: 2.0,
                color: Colors.white,
              ),
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildLoadingState() {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          const CircularProgressIndicator(
            color: Color(0xFF00F0FF),
          ),
          const SizedBox(height: 24),
          Text(
            'FORGING APPLICATION...',
            style: TextStyle(
              color: const Color(0xFF00F0FF).withOpacity(0.8),
              letterSpacing: 3.0,
              fontWeight: FontWeight.bold,
            ),
          ),
        ],
      ),
    );
  }
}
