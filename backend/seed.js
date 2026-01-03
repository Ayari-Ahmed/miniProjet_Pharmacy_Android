const mongoose = require('mongoose');
const User = require('./models/User');
const Pharmacy = require('./models/Pharmacy');
const Medicine = require('./models/Medicine');
require('dotenv').config();

// Sample data
const sampleMedicines = [
  {
    name: 'Paracetamol 500mg',
    genericName: 'Acetaminophen',
    description: 'Pain relief and fever reducer',
    category: 'Pain Relief',
    dosage: '500mg tablets',
    manufacturer: 'PharmaCorp',
    requiresPrescription: false,
    price: 5.50,
    sideEffects: ['Nausea', 'Skin rash'],
    usage: 'Take 1-2 tablets every 4-6 hours as needed'
  },
  {
    name: 'Amoxicillin 250mg',
    genericName: 'Amoxicillin',
    description: 'Antibiotic for bacterial infections',
    category: 'Antibiotics',
    dosage: '250mg capsules',
    manufacturer: 'MediPharm',
    requiresPrescription: true,
    price: 12.00,
    sideEffects: ['Diarrhea', 'Nausea', 'Allergic reactions'],
    usage: 'Take as directed by physician, usually 3 times daily'
  },
  {
    name: 'Ibuprofen 200mg',
    genericName: 'Ibuprofen',
    description: 'Non-steroidal anti-inflammatory drug',
    category: 'Pain Relief',
    dosage: '200mg tablets',
    manufacturer: 'HealthLabs',
    requiresPrescription: false,
    price: 8.75,
    sideEffects: ['Stomach upset', 'Heartburn'],
    usage: 'Take with food, 1-2 tablets every 4-6 hours'
  },
  {
    name: 'Cetirizine 10mg',
    genericName: 'Cetirizine',
    description: 'Antihistamine for allergies',
    category: 'Respiratory',
    dosage: '10mg tablets',
    manufacturer: 'AllergyCare',
    requiresPrescription: false,
    price: 10.00,
    sideEffects: ['Drowsiness', 'Dry mouth'],
    usage: 'Take 1 tablet daily'
  },
  {
    name: 'Vitamin C 1000mg',
    genericName: 'Ascorbic Acid',
    description: 'Vitamin C supplement for immune support',
    category: 'Vitamins',
    dosage: '1000mg tablets',
    manufacturer: 'NutriHealth',
    requiresPrescription: false,
    price: 8.00,
    sideEffects: ['Stomach upset (rare)'],
    usage: 'Take 1 tablet daily with food'
  },
  {
    name: 'Omeprazole 20mg',
    genericName: 'Omeprazole',
    description: 'Proton pump inhibitor for acid reflux',
    category: 'Digestive',
    dosage: '20mg capsules',
    manufacturer: 'DigestiveCare',
    requiresPrescription: true,
    price: 22.60,
    sideEffects: ['Headache', 'Diarrhea'],
    usage: 'Take 1 capsule daily before meals'
  }
];

const samplePharmacies = [
  {
    name: 'Pharmacie Centrale de Tunis',
    address: 'Avenue Habib Bourguiba, Tunis Medina',
    phone: '+216 71 123 456',
    email: 'centrale.pharmacy@email.com',
    password: 'pharmacy123',
    latitude: 36.7988,
    longitude: 10.1658,
    operatingHours: {
      monday: { open: '08:00', close: '20:00' },
      tuesday: { open: '08:00', close: '20:00' },
      wednesday: { open: '08:00', close: '20:00' },
      thursday: { open: '08:00', close: '20:00' },
      friday: { open: '08:00', close: '20:00' },
      saturday: { open: '09:00', close: '18:00' },
      sunday: { open: '10:00', close: '16:00' }
    },
    services: ['24/7', 'Home Delivery', 'Prescription'],
    licenseNumber: 'PHARM-001',
    pharmacistName: 'Dr. Sarah Johnson'
  },
  {
    name: 'Pharmacie Carthage',
    address: 'Byrsa Hill, Carthage',
    phone: '+216 71 234 567',
    email: 'carthage.pharmacy@email.com',
    password: 'pharmacy123',
    latitude: 36.8525,
    longitude: 10.3247,
    operatingHours: {
      monday: { open: '07:00', close: '22:00' },
      tuesday: { open: '07:00', close: '22:00' },
      wednesday: { open: '07:00', close: '22:00' },
      thursday: { open: '07:00', close: '22:00' },
      friday: { open: '07:00', close: '22:00' },
      saturday: { open: '08:00', close: '20:00' },
      sunday: { open: '09:00', close: '18:00' }
    },
    services: ['Home Delivery', 'Prescription', 'Consultation'],
    licenseNumber: 'PHARM-002',
    pharmacistName: 'Dr. Ahmed Ben Salah'
  },
  {
    name: 'Pharmacie Sidi Bou Said',
    address: 'Rue de la Mosquée, Sidi Bou Said',
    phone: '+216 71 345 678',
    email: 'sidi.pharmacy@email.com',
    password: 'pharmacy123',
    latitude: 36.8708,
    longitude: 10.3417,
    operatingHours: {
      monday: { open: '08:30', close: '19:30' },
      tuesday: { open: '08:30', close: '19:30' },
      wednesday: { open: '08:30', close: '19:30' },
      thursday: { open: '08:30', close: '19:30' },
      friday: { open: '08:30', close: '19:30' },
      saturday: { open: '09:00', close: '17:00' },
      sunday: { open: 'Closed', close: 'Closed' }
    },
    services: ['Prescription', 'Consultation'],
    licenseNumber: 'PHARM-003',
    pharmacistName: 'Dr. Leila Mansouri'
  }
];

const sampleUsers = [
  {
    name: 'Admin User',
    email: 'admin@hopemeds.com',
    password: 'admin123',
    role: 'admin',
    phone: '+216 71 000 000',
    address: 'Admin Office, Tunis'
  },
  {
    name: 'John Customer',
    email: 'john@example.com',
    password: 'password123',
    role: 'customer',
    phone: '+216 71 111 111',
    address: '123 Customer St, Tunis',
    latitude: 36.8065,
    longitude: 10.1815
  },
  {
    name: 'Ahmed Driver',
    email: 'ahmed.driver@example.com',
    password: 'driver123',
    role: 'driver',
    phone: '+216 71 222 222',
    address: '456 Driver Ave, Tunis',
    latitude: 36.8000,
    longitude: 10.1850
  }
];

async function seedDatabase() {
  try {
    // Connect to MongoDB
    await mongoose.connect(process.env.MONGODB_URI || 'mongodb+srv://Ayari:ta5wira55@ayari-list.zjtgaz3.mongodb.net/HopeMeds_', {
      useNewUrlParser: true,
      useUnifiedTopology: true,
    });

    console.log('Connected to MongoDB');

    // Clear existing data
    await User.deleteMany({});
    await Pharmacy.deleteMany({});
    await Medicine.deleteMany({});

    console.log('Cleared existing data');

    // Insert medicines
    const medicines = await Medicine.insertMany(sampleMedicines);
    console.log(`Inserted ${medicines.length} medicines`);

    // Insert pharmacies with stock
    for (const pharmacyData of samplePharmacies) {
      const stock = medicines.map(medicine => ({
        medicine: medicine._id,
        stock: Math.floor(Math.random() * 50) + 10, // Random stock 10-60
        price: medicine.price * (0.9 + Math.random() * 0.2), // Price variation ±10%
        lastUpdated: new Date()
      }));

      const pharmacy = new Pharmacy({
        ...pharmacyData,
        stock: stock
      });

      await pharmacy.save();
    }
    console.log(`Inserted ${samplePharmacies.length} pharmacies`);

    // Insert users
    await User.insertMany(sampleUsers);
    console.log(`Inserted ${sampleUsers.length} users`);

    console.log('Database seeded successfully!');
    console.log('\nSample login credentials:');
    console.log('Admin: admin@hopemeds.com / admin123');
    console.log('Customer: john@example.com / password123');
    console.log('Driver: ahmed.driver@example.com / driver123');
    console.log('Pharmacies: centrale.pharmacy@email.com / pharmacy123');
    console.log('           carthage.pharmacy@email.com / pharmacy123');
    console.log('           sidi.pharmacy@email.com / pharmacy123');

  } catch (error) {
    console.error('Error seeding database:', error);
  } finally {
    await mongoose.connection.close();
    console.log('Database connection closed');
  }
}

// Run seeder if called directly
if (require.main === module) {
  seedDatabase();
}

module.exports = seedDatabase;